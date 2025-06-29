package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.*;
import network.venox.cobalt.CoUtilities;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.StringUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
        if (channelType.equals(ChannelType.PRIVATE)) return;

        // TextChannel
        if (channelType.equals(ChannelType.TEXT)) onTextChannelReceived(event);

        // Guild
        final Member member = event.getMember();
        if (member == null) return;
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final CoGuild coGuild = bot.oldData.getGuild(guild);
        final GuildMessageChannel guildChannel = event.getGuildChannel();
        final long channelId = guildChannel.getIdLong();
        final String authorName = author.getName();
        final Message message = event.getMessage();
        final MessageType type = message.getType();

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
        if (stickyMessage != null) stickyMessage.send();

        // Limited messages
        final CoLimitedMessages limitedMessages = coGuild.getLimitedMessages(channelId);
        if (limitedMessages != null) limitedMessages.processMessage(message);

        // Auto delete
        final Set<Long> autoDelete = coGuild.autoDeletes.get(channelId);
        if (autoDelete != null && member.getRoles().stream()
                .map(Role::getIdLong)
                .noneMatch(autoDelete::contains)) message.delete().queue();

        // AFK (disable)
        final CoUser coUser = bot.oldData.getUser(author);
        if (coUser.afk()) message.reply(":wave: **Welcome back,** you are no longer AFK!").queue(s -> coUser.afk = false);

        // User data
        if (!guild.getSelfMember().hasPermission(guildChannel, Permission.MESSAGE_HISTORY)) return;
        final long authorId = author.getIdLong();
        final String content = message.getContentRaw();
        final String contentLower = content.toLowerCase().replaceAll("https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,63}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]{0,63}", "");
        final String jumpUrl = message.getJumpUrl();
        final Set<Long> mentions = message.getMentions().getUsers().stream()
                .map(ISnowflake::getIdLong)
                .collect(Collectors.toSet());
        final OffsetDateTime time = OffsetDateTime.now().minus(CoUser.HIGHLIGHT_TIME, ChronoUnit.MILLIS);
        final MessageHistory.MessageRetrieveAction historyAction = message.getChannel().getHistoryBefore(message, 4);
        for (final CoUser otherCoUser : bot.oldData.users) {
            if (otherCoUser.id == authorId) continue;
            final CacheRestAction<User> otherUserAction = otherCoUser.getUser();

            // Check if mentioned
            if (mentions.stream().anyMatch(streamId -> streamId == otherCoUser.userId)) {
                // AFK (alert)
                if (otherCoUser.afk()) otherUserAction
                        .flatMap(otherUser -> message.reply(LazyEmoji.WARNING + " **`" + otherUser.getName() + "`** is currently AFK!"))
                        .queue();
                continue;
            }

            // Highlights
            if (content.length() < 500) otherCoUser.getMember(guild).queue(coMember -> {
                if (coMember.hasPermission(guildChannel, Permission.MESSAGE_HISTORY)) CoUtilities.userTalkedOrMentionedRecently(otherCoUser.userId, guildChannel, time).queue(talkedRecently -> {
                    if (talkedRecently == null || talkedRecently) return;
                    // Check if in an audio channel
                    final GuildVoiceState voiceState = coMember.getVoiceState();
                    if (voiceState != null && voiceState.inAudioChannel()) return;
                    // Check cooldown
                    final Long cooldown = otherCoUser.highlightCooldowns.get(guildId);
                    if (cooldown != null) {
                        if (cooldown - System.currentTimeMillis() > 0) return;
                        otherCoUser.highlightCooldowns.remove(guildId);
                    }
                    // Check highlights
                    for (final String highlight : otherCoUser.highlights) {
                        final int index = contentLower.indexOf(highlight);
                        if (index == -1) continue;
                        // Add to cooldowns
                        otherCoUser.highlightCooldowns.put(guildId, System.currentTimeMillis() + CoUser.HIGHLIGHT_TIME);
                        // Send embed in DMs
                        final LazyEmbed embed = new LazyEmbed()
                                .setAuthor(authorName, "https://discord.com/users/" + author.getId(), author.getEffectiveAvatarUrl())
                                .setTitle(highlight, jumpUrl)
                                .setFooter("#" + message.getChannel().getName() + " in " + message.getGuild().getName(), message.getGuild().getIconUrl())
                                .setTimestamp(message.getTimeCreated());
                        historyAction
                                .flatMap(history -> {
                                    // Previous messages
                                    final List<Message> messages = new ArrayList<>(history.getRetrievedHistory());
                                    Collections.reverse(messages);
                                    messages.forEach(msg -> embed.addField(msg.getAuthor().getName(), StringUtility.shorten(msg.getContentRaw(), 1024), false));
                                    // Highlighted message
                                    embed.addField(authorName, StringUtility.shorten(content.substring(0, index) +
                                            "[" + content.substring(index, index + highlight.length()) + "](" + jumpUrl + ")" +
                                            content.substring(index + highlight.length()), 1024), false);
                                    return otherUserAction;
                                })
                                .flatMap(User::openPrivateChannel)
                                .flatMap(channel -> channel.sendMessageEmbeds(embed.build(bot)))
                                .queue();
                        break;
                    }
                });
            }, f -> {});
        }
    }

    private void onTextChannelReceived(@NotNull MessageReceivedEvent event) {
        // Slowmode
        final TextChannel channel = event.getGuildChannel().asTextChannel();
        final CoSlowmode slowmode = bot.oldData.getGuild(event.getGuild()).getSlowmode(channel.getIdLong());
        if (slowmode != null) slowmode.setSlowmode(channel);
    }
}
