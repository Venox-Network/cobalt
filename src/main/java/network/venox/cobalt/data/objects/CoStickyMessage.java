package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CoStickyMessage extends CoObject {
    @NotNull private final Cobalt cobalt;
    private final long guildId;

    public final long channel;
    @NotNull public LazyMessage message;
    @Nullable public Long current;
    @Nullable private ScheduledFuture<?> future;

    public CoStickyMessage(@NotNull Cobalt cobalt, long guildId, long channel, @NotNull LazyMessage message, @Nullable Long current) {
        this.cobalt = cobalt;
        this.guildId = guildId;
        this.channel = channel;
        this.message = message;
        this.current = current;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("message", message.toMap());
        if (current != null) map.put("current", current);
        return map;
    }

    @Override
    public boolean isNull() {
        return getChannel() == null;
    }

    @Nullable
    public Guild getGuild() {
        return cobalt.jda.getGuildById(guildId);
    }

    @Nullable
    public TextChannel getChannel() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(channel);
    }

    @Nullable
    public CompletableFuture<Message> getCurrent() {
        final TextChannel textChannel = getChannel();
        if (textChannel == null) return null;
        if (current == null) return findCurrent(textChannel);

        // Return current message if it exists
        final Message newMessage = MiscUtility.handleException(() -> textChannel.retrieveMessageById(current).complete());
        if (newMessage != null) return CompletableFuture.completedFuture(newMessage);

        // Current message doesn't exist, try to find it
        return findCurrent(textChannel);
    }

    @Nullable
    private CompletableFuture<Message> findCurrent(@NotNull TextChannel textChannel) {
        // Search for the message in the channel's history (last 10 messages)
        // Checks: Author is the bot, content is the same, embeds are the same
        return textChannel.getIterableHistory()
                .takeAsync(10)
                .thenApply(messages -> messages.stream()
                        .filter(iterableMessage -> iterableMessage.getAuthor().equals(textChannel.getJDA().getSelfUser()) && iterableMessage.getContentRaw().equals(message.content))
                        .filter(iterableMessage -> iterableMessage.getEmbeds().equals(message.getBuiltEmbeds(cobalt)))
                        .findFirst()
                        .orElse(null));
    }

    public void send() {
        final TextChannel textChannel = getChannel();
        if (textChannel == null) return;

        // Delete current message
        delete();

        // Schedule message to be sent
        if (future != null) future.cancel(true);
        future = Cobalt.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            textChannel.sendMessage(message.toBuilder(cobalt).build()).queue(msg -> current = msg.getIdLong());
            future = null;
        }, 3, TimeUnit.SECONDS);
    }

    public void delete() {
        final CompletableFuture<Message> currentMessage = getCurrent();
        if (currentMessage != null) currentMessage.thenAcceptAsync(msg -> msg.delete().queue());
    }
}
