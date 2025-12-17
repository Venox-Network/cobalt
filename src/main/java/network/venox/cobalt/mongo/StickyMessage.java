package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import network.venox.cobalt.MongoProvider;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class StickyMessage {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MESSAGE = "message";
    @NotNull public static final String PROP_CURRENT = "current";

    /**
     * [channel ID, future]
     */
    @NotNull private static final Map<Long, ScheduledFuture<?>> STICKY_FUTURES = new HashMap<>();

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MESSAGE) public MongoMessage message;
    @BsonProperty(PROP_CURRENT) @Nullable public Long current;

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @NotNull
    public Optional<TextChannel> channel(@NotNull JDA jda) {
        return guild(jda).map(guild -> guild.getTextChannelById(channel));
    }

    public void send(@NotNull MongoProvider mongo, @NotNull MessageChannel messageChannel) {
        // Delete current message
        delete(messageChannel);
        if (current != null) mongo.database.getMagicCollection(StickyMessage.class).updateOne(
                Filters.eq("_id", channel),
                Updates.unset(PROP_CURRENT));

        // Schedule message to be sent
        final ScheduledFuture<?> future = STICKY_FUTURES.get(channel);
        if (future != null) future.cancel(true);
        STICKY_FUTURES.put(channel, MiscUtility.IO_SCHEDULER.schedule(() -> {
            messageChannel.sendMessage(message.toBuilder().build())
                    .queue(msg -> mongo.database.getMagicCollection(StickyMessage.class).updateOne(
                            Filters.eq("_id", channel),
                            Updates.set(PROP_CURRENT, msg.getIdLong())));
            STICKY_FUTURES.remove(channel);
        }, 1, TimeUnit.MINUTES));
    }

    public void delete(@NotNull MessageChannel textChannel) {
        if (current != null) textChannel.retrieveMessageById(current)
                .flatMap(Message::delete)
                .queue(null, LazyUtilities.IGNORE_UNKNOWN_MESSAGE);
    }
}
