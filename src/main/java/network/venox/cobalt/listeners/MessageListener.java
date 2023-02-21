package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.CoUser;
import network.venox.cobalt.data.objects.*;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

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
        final GuildMessageChannel channel = CoMapper.handleException(() -> event.getChannel().asGuildMessageChannel());
        final User author = event.getAuthor();
        if (channel == null || author.isBot()) return;
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final long channelId = channel.getIdLong();
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
        if (channel instanceof TextChannel text) {
            final CoSlowmode slowmode = coGuild.getSlowmode(channelId);
            if (slowmode != null) slowmode.setSlowmode(text);
        }

        // Limited messages
        final CoLimitedMessages limitedMessages = coGuild.getLimitedMessages(channelId);
        if (limitedMessages != null) limitedMessages.processMessage(message);

        // Highlights
        final long authorId = author.getIdLong();
        final Set<String> words = Arrays.stream(message.getContentRaw().split(" "))
                .map(word -> word.toLowerCase().trim())
                .collect(Collectors.toSet());
        for (final CoUser user : cobalt.data.users) {
            if (user.userId == authorId) continue;
            final Member member = guild.getMemberById(user.userId);
            if (member == null || !member.hasPermission(channel, Permission.MESSAGE_HISTORY) || memberTalkedRecently(member, channel)) continue;
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
