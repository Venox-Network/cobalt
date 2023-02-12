package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CoStickyMessage implements CoObject {
    public final long channel;
    @NotNull private final CoMessage message;
    @Nullable private Long current;
    @Nullable private ScheduledFuture<?> future;

    public CoStickyMessage(long channel, @NotNull CoMessage message, @Nullable Long current) {
        this.channel = channel;
        this.message = message;
        this.current = current;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("message", message.toMap());
        map.put("current", current);
        return map;
    }

    @Nullable
    public TextChannel getChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(channel);
    }

    @Nullable
    public Message getCurrent(@NotNull Guild guild) {
        final TextChannel textChannel = getChannel(guild);
        if (textChannel == null) return null;
        if (current == null) return findCurrent(textChannel);

        // Return current message if it exists
        final Message newMessage = CoMapper.handleException(() -> textChannel.retrieveMessageById(current).complete());
        if (newMessage != null) return newMessage;

        // Current message doesn't exist, try to find it
        return findCurrent(textChannel);
    }

    @Nullable
    private Message findCurrent(@NotNull TextChannel textChannel) {
        // Search for the message in the channel's history (last 10 messages)
        // Checks: Author is the bot, content is the same, embeds are the same
        int i = 0;
        for (final Message iterableMessage : textChannel.getIterableHistory()) {
            if (i > 10) break;
            if (iterableMessage.getAuthor().equals(textChannel.getJDA().getSelfUser()) && iterableMessage.getContentRaw().equals(message.content)) {
                final List<MessageEmbed> embeds = message.embeds.stream()
                        .map(CoEmbed::build)
                        .toList();
                if (iterableMessage.getEmbeds().equals(embeds)) return iterableMessage;
            }
            i++;
        }

        // Message not found
        return null;
    }

    public void send(@NotNull Cobalt cobalt, @NotNull Guild guild) {
        final TextChannel textChannel = getChannel(guild);
        if (textChannel == null) return;

        // Delete current message
        delete(guild);

        // Schedule message to be sent
        if (future != null) future.cancel(true);
        future = cobalt.scheduledExecutorService.schedule(() -> {
            current = textChannel.sendMessage(message.toBuilder().build()).complete().getIdLong();
            future = null;
        }, 5, TimeUnit.SECONDS);
    }

    public void delete(@NotNull Guild guild) {
        final Message currentMessage = getCurrent(guild);
        if (currentMessage != null) currentMessage.delete().queue();
    }
}
