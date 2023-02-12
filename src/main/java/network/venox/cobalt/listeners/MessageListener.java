package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
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

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class MessageListener extends CoListener {
    public MessageListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final TextChannel channel = event.getChannel().asTextChannel();
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
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        for (final CoUser user : cobalt.data.users) for (final String highlight : user.highlights) if (words.contains(highlight)) {
            user.sendHighlight(highlight, message);
            break;
        }
    }
}
