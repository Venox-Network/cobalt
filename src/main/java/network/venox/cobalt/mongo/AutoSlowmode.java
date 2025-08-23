package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import java.time.Duration;
import java.util.*;


public class AutoSlowmode {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MINIMUM = "minimum";
    @NotNull public static final String PROP_MAXIMUM = "maximum";
    @NotNull public static final String PROP_LAST_CHECK = "last_check";

    @NotNull
    private static final Duration DELAY = Duration.ofMinutes(1);
    /**
     * [channel ID, [user ID, last active time]]
     */
    @NotNull public static final Map<Long, Map<Long, Long>> ACTIVE_USERS = new HashMap<>();

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MINIMUM) public int minimum;
    @BsonProperty(PROP_MAXIMUM) public int maximum;
    @BsonProperty(PROP_LAST_CHECK) @Nullable public Date lastCheck;

    public void setSlowmode(@NotNull Cobalt bot, @NotNull TextChannel textChannel) {
        final long now = System.currentTimeMillis();
        final long delayAgo = now - DELAY.toMillis();

        // Check if slowmode has been set recently
        if (lastCheck != null && lastCheck.getTime() > delayAgo) return;

        // Update lastCheck
        bot.mongo.getMagicCollection(AutoSlowmode.class).updateOne(
                Filters.eq("_id", channel),
                Updates.set(PROP_LAST_CHECK, new Date(now)));

        // Get the users active in the last DELAY
        final Map<Long, Long> activeUsers = ACTIVE_USERS.get(channel);
        if (activeUsers == null) return;
        activeUsers.entrySet().removeIf(entry -> entry.getValue() < delayAgo);

        // Calculate and set slowmode
        Mapper.toInt(Math.max(minimum, Math.min(activeUsers.size(), maximum)))
                .ifPresent(slowmode -> {
                    if (slowmode != textChannel.getSlowmode()) textChannel.getManager().setSlowmode(slowmode).queue();
                });
    }
}
