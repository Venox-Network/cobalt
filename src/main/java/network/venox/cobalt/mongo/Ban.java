package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


public class Ban {
    @BsonId public ObjectId id;
    @BsonProperty("user") public long userId;
    public String reason;
    @Nullable public Long time;
    @BsonProperty("moderator") public long moderatorId;

    public Ban() {}

    public Ban(long userId, @NotNull String reason, @Nullable Long time, long moderatorId) {
        this.userId = userId;
        this.reason = reason;
        this.time = time;
        this.moderatorId = moderatorId;
    }

    @NotNull
    public CacheRestAction<User> getUser(@NotNull JDA jda) {
        return jda.retrieveUserById(userId);
    }

    @NotNull
    public CacheRestAction<User> getModerator(@NotNull JDA jda) {
        return jda.retrieveUserById(moderatorId);
    }

    public boolean isExpired() {
        return time != null && time < System.currentTimeMillis();
    }

    @NotNull
    public String getTimeLeft() {
        if (time == null) return "Permanent";
        final Duration duration = Duration.of(time - System.currentTimeMillis(), ChronoUnit.MILLIS);

        // Get times
        final long years = duration.toDays() / 365;
        final long months = duration.toDays() / 30;
        final long weeks = duration.toDays() / 7;
        final long days = duration.toDays();
        final long hours = duration.minusDays(days).toHours();
        final long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        final long seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();

        // Build string
        final StringBuilder builder = new StringBuilder();
        if (years >= 1) builder.append(years).append("y ");
        if (months >= 1) builder.append(months).append("mo ");
        if (weeks >= 1) builder.append(weeks).append("w ");
        if (days >= 1) builder.append(days).append("d ");
        if (hours >= 1) builder.append(hours).append("h ");
        if (minutes >= 1) builder.append(minutes).append("m ");
        if (seconds >= 1) builder.append(seconds).append("s ");
        if (builder.isEmpty()) builder.append("0s");
        return builder.toString();
    }

    public void ban(@NotNull JDA jda) {
        getUser(jda).queue(userJda -> {
            for (final Guild guild : jda.getMutualGuilds(userJda)) {
                final Member member = guild.getMember(userJda);
                if (member != null && guild.getSelfMember().canInteract(member)) guild.ban(member, 1, TimeUnit.DAYS).reason(reason).queue(s -> {}, f -> {});
            }
        });
    }

    public void unban(@NotNull JDA jda) {
        getUser(jda).queue(userJda -> {
            for (final Guild guild : jda.getGuilds()) guild.unban(userJda).queue();
        });
    }
}
