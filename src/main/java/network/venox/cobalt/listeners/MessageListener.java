package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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
        if (author.isBot()) return;
        final ChannelType channelType = event.getChannel().getType();

        // PrivateChannel
        if (channelType.equals(ChannelType.PRIVATE)) {
            onPrivateChannelReceived(event);
            return;
        }

        // TextChannel
        if (channelType.equals(ChannelType.TEXT)) onTextChannelReceived(event);

        // ThreadChannel
        if (channelType.equals(ChannelType.GUILD_PUBLIC_THREAD) || channelType.equals(ChannelType.GUILD_PRIVATE_THREAD)) onThreadChannelReceived(event);

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

        // Limited messages
        final CoLimitedMessages limitedMessages = coGuild.getLimitedMessages(channelId);
        if (limitedMessages != null) limitedMessages.processMessage(message);

        // Auto delete
        final Set<Long> autoDelete = coGuild.autoDeletes.get(channelId);
        if (autoDelete != null && member.getRoles().stream()
                .map(Role::getIdLong)
                .noneMatch(autoDelete::contains)) message.delete().queue();

        // Highlights
        final long authorId = author.getIdLong();
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

    private void onTextChannelReceived(@NotNull MessageReceivedEvent event) {
        final TextChannel channel = event.getGuildChannel().asTextChannel();
        final CoSlowmode slowmode = cobalt.data.getGuild(event.getGuild()).getSlowmode(channel.getIdLong());
        if (slowmode != null) slowmode.setSlowmode(channel);
    }

    private void onThreadChannelReceived(@NotNull MessageReceivedEvent event) {
        final ThreadChannel channel = event.getGuildChannel().asThreadChannel();
        final CacheRestAction<User> userAction = cobalt.data.global.getModmailUser(channel.getIdLong());
        if (userAction == null) return;
        final MessageCreateBuilder builder = MessageCreateBuilder.fromMessage(event.getMessage());
        builder.setContent("**" + event.getAuthor().getAsMention() + ":**\n" + builder.getContent());
        userAction
                .flatMap(User::openPrivateChannel)
                .flatMap(privateChannel -> privateChannel.sendMessage(builder.build()))
                .queue();
    }

    private void onPrivateChannelReceived(@NotNull MessageReceivedEvent event) {
        final User author = event.getAuthor();
        final long authorId = author.getIdLong();

        // Embed
        final EmbedCmd.Data data = cobalt.embedBuilders.get(authorId);
        if (data != null) {
            if (data.parameter == null) return;
            final PrivateChannel channel = event.getChannel().asPrivateChannel();
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

        // Global modmail
        final ThreadChannel thread = cobalt.data.global.getModmailThread(authorId);
        final MessageCreateBuilder builder = MessageCreateBuilder.fromMessage(event.getMessage());
        if (builder.getContent().isEmpty()) return;
        // Create new thread
        if (thread == null) {
            cobalt.data.global.sendModmailConfirmation(author, builder).queue();
            return;
        }
        // Send message to existing thread
        thread.sendMessage(builder.build()).queue();
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
