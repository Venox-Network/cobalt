package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import java.time.OffsetDateTime;
import java.util.*;


public class AutoSlowmode {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MINIMUM = "minimum";
    @NotNull public static final String PROP_MAXIMUM = "maximum";
    @NotNull public static final String PROP_LAST_CHECK = "last_check";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MINIMUM) public int minimum;
    @BsonProperty(PROP_MAXIMUM) public int maximum;
    @BsonProperty(PROP_LAST_CHECK) @Nullable public Date lastCheck;

    public void setSlowmode(@NotNull Cobalt bot, @NotNull TextChannel textChannel) {
        final long now = System.currentTimeMillis();

        // Check if slowmode has been set recently
        if (lastCheck != null && lastCheck.after(new Date(now - 15000))) return;

        // Get the users in chat sent since last check or 15 seconds ago
        int total = 0;
        final Set<Long> users = new HashSet<>();
        final OffsetDateTime time = OffsetDateTime.now().minusSeconds(lastCheck == null ? 15 : ((now - lastCheck.getTime()) / 1000));
        for (final Message message : textChannel.getIterableHistory()) {
            total++;
            if (total > maximum || message.getTimeCreated().isBefore(time)) break;
            final User author = message.getAuthor();
            if (!author.isBot()) users.add(author.getIdLong());
        }

        // Update lastCheck
        bot.mongo.getMagicCollection(AutoSlowmode.class).updateOne(
                Filters.eq("_id", channel),
                Updates.set(PROP_LAST_CHECK, new Date()));

        // Calculate and set slowmode
        Mapper.toInt(Math.max(minimum, Math.min(maximum, users.size())))
                .ifPresent(slowmode -> textChannel.getManager().setSlowmode(slowmode).queue());
    }
}
