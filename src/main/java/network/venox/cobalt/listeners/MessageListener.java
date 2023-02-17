package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.CoUser;
import network.venox.cobalt.data.objects.CoReactChannel;
import network.venox.cobalt.data.objects.CoSlowmode;
import network.venox.cobalt.data.objects.CoStickyMessage;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public class MessageListener extends CoListener {
    public MessageListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final TextChannel channel = CoMapper.handleException(() -> event.getChannel().asTextChannel());
        if (channel == null || event.getAuthor().isBot()) return;
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final long channelId = channel.getIdLong();
        final Message message = event.getMessage();

        // React channel
        final CoReactChannel reactChannel = coGuild.getReactChannel(channelId);
        if (reactChannel != null) reactChannel.addReactions(message);

        // Thread channel
        if (coGuild.threadChannels.contains(channelId)) message.createThreadChannel(message.getContentStripped()).queue();

        // Sticky message
        final CoStickyMessage stickyMessage = coGuild.getStickyMessage(channelId);
        if (stickyMessage != null) stickyMessage.send(cobalt, guild);

        // Slowmode
        final CoSlowmode slowmode = coGuild.getSlowmode(channelId);
        if (slowmode != null) slowmode.setSlowmode(channel);

        // Highlights
        final Set<String> words = Arrays.stream(message.getContentRaw().split(" "))
                .map(word -> word.toLowerCase().trim())
                .collect(Collectors.toSet());
        for (final CoUser user : cobalt.data.users) {
            final Member member = guild.getMemberById(user.userId);
            if (member != null && member.hasPermission(channel, Permission.MESSAGE_HISTORY)) for (final String highlight : user.highlights) if (words.contains(highlight)) {
                user.sendHighlight(highlight, message);
                break;
            }
        }
    }
}
