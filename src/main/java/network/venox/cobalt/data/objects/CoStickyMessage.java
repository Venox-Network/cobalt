package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CoStickyMessage implements CoObject {
    public final long channel;
    @NotNull
    public CoMessage message;
    @Nullable
    public Long current;
    @Nullable private ScheduledFuture<?> future;

    public CoStickyMessage(long channel, @NotNull CoMessage message, @Nullable Long current) {
        this.channel = channel;
        this.message = message;
        this.current = current;
    }

    @Nullable
    public TextChannel getChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(channel);
    }

    @Nullable
    public CompletableFuture<Message> getCurrent(@NotNull Guild guild) {
        final TextChannel textChannel = getChannel(guild);
        if (textChannel == null) return null;
        if (current == null) return findCurrent(textChannel);

        // Return current message if it exists
        final Message newMessage = CoMapper.handleException(() -> textChannel.retrieveMessageById(current).complete());
        if (newMessage != null) return CompletableFuture.completedFuture(newMessage);

        // Current message doesn't exist, try to find it
        return findCurrent(textChannel);
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("message", message.toMap());
        map.put("current", current);
        return map;
    }

    @Nullable
    private CompletableFuture<Message> findCurrent(@NotNull TextChannel textChannel) {
        // Search for the message in the channel's history (last 10 messages)
        // Checks: Author is the bot, content is the same, embeds are the same
        return textChannel.getIterableHistory()
                .takeAsync(10)
                .thenApply(messages -> messages.stream()
                        .filter(iterableMessage -> iterableMessage.getAuthor().equals(textChannel.getJDA().getSelfUser()) && iterableMessage.getContentRaw().equals(message.content))
                        .filter(iterableMessage -> iterableMessage.getEmbeds().equals(message.getBuiltEmbeds()))
                        .findFirst()
                        .orElse(null));
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
        }, 3, TimeUnit.SECONDS);
    }

    public void delete(@NotNull Guild guild) {
        final CompletableFuture<Message> currentMessage = getCurrent(guild);
        if (currentMessage != null) currentMessage.thenAcceptAsync(msg -> msg.delete().queue());
    }
}
