package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.global.EmbedCmd;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.CoUser;
import network.venox.cobalt.data.objects.*;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public class MessageListener extends CoListener {
    public MessageListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final User author = event.getAuthor();
        final long authorId = author.getIdLong();
        if (author.isBot()) return;
        final MessageChannelUnion channelUnion = event.getChannel();

        // DM
        if (channelUnion.getType().equals(ChannelType.PRIVATE)) {
            // Embed
            final EmbedCmd.Data data = cobalt.embedBuilders.get(authorId);
            if (data == null || data.parameter == null) return;
            final PrivateChannel channel = channelUnion.asPrivateChannel();
            final String value = event.getMessage().getContentRaw();
            data.getEmbedMessage(channel).queue(message -> {
                final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().get(0));
                switch (data.parameter) {
                    case "color" -> builder.setColor(Color.decode(value));
                    case "author" -> {
                        final String[] split = value.split("==", 2);
                        builder.setAuthor(split[0], split.length > 1 ? split[1] : null);
                    }
                    case "title" -> {
                        final String[] split = value.split("==", 2);
                        builder.setTitle(split[0], split.length > 1 ? split[1] : null);
                    }
                    case "description" -> builder.setDescription(value);
                    case "field" -> {
                        final String[] split = value.split("==", 3);
                        if (split.length < 2) {
                            channel.sendMessage("Invalid field format, please use `name||value` or `name||value||inline`").queue();
                            return;
                        }
                        builder.addField(split[0], split[1], split.length > 2 && Boolean.parseBoolean(split[2]));
                    }
                    case "thumbnail" -> builder.setThumbnail(value);
                    case "image" -> builder.setImage(value);
                    case "footer" -> {
                        final String[] split = value.split("==", 2);
                        builder.setFooter(split[0], split.length > 1 ? split[1] : null);
                    }
                    case "timestamp" -> {
                        if (value.equals("now")) {
                            builder.setTimestamp(OffsetDateTime.now());
                        } else {
                            final Long timestamp = CoMapper.toLong(value);
                            if (timestamp == null) {
                                channel.sendMessage("Invalid timestamp, please use a valid epoch timestamp").queue();
                                return;
                            }
                            builder.setTimestamp(Instant.ofEpochMilli(timestamp));
                        }
                    }
                    default -> channel.sendMessage("Unknown parameter `" + data.parameter + "`").queue();
                }

                // Send new embed
                channel.sendMessageEmbeds(builder.build())
                        .addActionRow(EmbedCmd.selectMenu())
                        .addActionRow(EmbedCmd.getButtons(cobalt, author, data.channel))
                        .queue(newMessage -> data.embedMessage = newMessage.getIdLong());

                // Delete old messages
                message.delete().queue();
                final InteractionHook hook = data.parameterHook;
                if (hook != null) hook.deleteOriginal().queue();
            });
            return;
        }

        // Guild
        final Member member = event.getMember();
        if (member == null) return;
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final GuildMessageChannel guildChannel = event.getGuildChannel();
        final long channelId = guildChannel.getIdLong();
        final Message message = event.getMessage();
        final MessageType type = message.getType();

        // Welcome channel
        final Long welcomeChannel = coGuild.welcomeChannel;
        if (welcomeChannel != null && channelId == welcomeChannel && type.equals(MessageType.GUILD_MEMBER_JOIN)) message.addReaction(Emoji.fromUnicode("U+1F44B")).queue();

        // React channel
        final CoReactChannel reactChannel = coGuild.getReactChannel(channelId);
        if (reactChannel != null) reactChannel.addReactions(message);

        // Thread channel
        if (!type.equals(MessageType.THREAD_CREATED)) {
            final CoThreadChannel threadChannel = coGuild.getThreadChannel(channelId);
            if (threadChannel != null) threadChannel.createThread(message);
        }

        // Sticky message
        final CoStickyMessage stickyMessage = coGuild.getStickyMessage(channelId);
        if (stickyMessage != null) stickyMessage.send(guild);

        // Slowmode
        if (guildChannel instanceof TextChannel text) {
            final CoSlowmode slowmode = coGuild.getSlowmode(channelId);
            if (slowmode != null) slowmode.setSlowmode(text);
        }

        // Limited messages
        final CoLimitedMessages limitedMessages = coGuild.getLimitedMessages(channelId);
        if (limitedMessages != null) limitedMessages.processMessage(message);

        // Auto delete
        final Set<Long> autoDelete = coGuild.autoDeletes.get(channelId);
        if (autoDelete != null && member.getRoles().stream()
                .map(Role::getIdLong)
                .noneMatch(autoDelete::contains)) message.delete().queue();

        // Highlights
        final Set<String> words = Arrays.stream(message.getContentRaw().split(" "))
                .map(word -> word.toLowerCase().trim())
                .collect(Collectors.toSet());
        for (final CoUser user : cobalt.data.users) {
            if (user.userId == authorId) continue;
            final Member coMember = guild.getMemberById(user.userId);
            if (coMember == null || !coMember.hasPermission(guildChannel, Permission.MESSAGE_HISTORY) || memberTalkedRecently(coMember, guildChannel)) continue;
            for (final String highlight : user.highlights) {
                if (!words.contains(highlight)) continue;
                user.sendHighlight(highlight, message);
                break;
            }
        }
    }

    private boolean memberTalkedRecently(@NotNull Member member, @NotNull GuildMessageChannel channel) {
        final long memberId = member.getIdLong();
        final OffsetDateTime threeMinutesAgo = OffsetDateTime.now().minusMinutes(3);
        for (final Message message : channel.getHistory().retrievePast(50).complete()) {
            if (message.getTimeCreated().isBefore(threeMinutesAgo)) break;
            if (message.getAuthor().getIdLong() == memberId) return true;
        }
        return false;
    }
}
