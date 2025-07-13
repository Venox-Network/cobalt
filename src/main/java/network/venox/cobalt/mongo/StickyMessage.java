package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyMessage;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class StickyMessage {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MESSAGE = "message";
    @NotNull public static final String PROP_CURRENT = "current";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MESSAGE) public LazyMessage message; //TODO
    @BsonProperty(PROP_CURRENT) @Nullable public Long current;

    public StickyMessage() {}

    public StickyMessage(@NotNull Message message) {
        this.guild = message.getGuildIdLong();
        this.channel = message.getChannelIdLong();
        this.message = new LazyMessage(message);
    }

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @NotNull
    public Optional<TextChannel> channel(@NotNull JDA jda) {
        return guild(jda).map(guild -> guild.getTextChannelById(channel));
    }

    @NotNull
    public Optional<CompletableFuture<Message>> current(@NotNull Cobalt bot) {
        final TextChannel textChannel = channel(bot.jda).orElse(null);
        if (textChannel == null) return Optional.empty();
        if (current == null) return Optional.of(findCurrent(bot, textChannel));
        return MiscUtility.handleException(() -> textChannel.retrieveMessageById(current).complete())
                .map(CompletableFuture::completedFuture)
                .or(() -> Optional.of(findCurrent(bot, textChannel)));
    }

    @NotNull
    private CompletableFuture<Message> findCurrent(@NotNull Cobalt bot, @NotNull TextChannel textChannel) {
        // Search for the message in the channel's history (last 10 messages)
        // Checks: Author is the bot, content is the same, embeds are the same
        return textChannel.getIterableHistory()
                .takeAsync(10)
                .thenApply(messages -> messages.stream()
                        .filter(iterableMessage -> iterableMessage.getAuthor().equals(textChannel.getJDA().getSelfUser()) && iterableMessage.getContentRaw().equals(message.content))
                        .filter(iterableMessage -> iterableMessage.getEmbeds().equals(message.getBuiltEmbeds(bot)))
                        .findFirst()
                        .orElse(null));
    }

    public void send(@NotNull Cobalt bot, @NotNull MessageChannel guildChannel) {
        // Delete current message
        delete(bot);

        // Schedule message to be sent
        final ScheduledFuture<?> future = bot.dataManager.stickyFutures.get(channel);
        if (future != null) future.cancel(true);
        bot.dataManager.stickyFutures.put(channel, LazyUtilities.IO_SCHEDULER.schedule(() -> {
            guildChannel.sendMessage(message.toBuilder(bot).build()).queue(msg -> current = msg.getIdLong());
            bot.dataManager.stickyFutures.remove(channel);
        }, 3, TimeUnit.SECONDS));
    }

    public void delete(@NotNull Cobalt bot) {
        current(bot).ifPresent(messageCompletableFuture -> messageCompletableFuture.thenAcceptAsync(msg -> msg.delete().queue()));
    }
}
