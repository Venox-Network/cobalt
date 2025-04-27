package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public final class CoSuperBan extends CoObject {
    @NotNull private final JDA jda;

    public final long user;
    @NotNull public final String reason;
    @Nullable public final Long time;
    public final long moderator;

    public CoSuperBan(@NotNull JDA jda, long user, @NotNull String reason, @Nullable Long time, long moderator) {
        this.jda = jda;
        this.user = user;
        this.reason = reason;
        this.time = time;
        this.moderator = moderator;
    }

    @Override
    @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("reason", reason);
        if (time != null) map.put("time", time);
        map.put("moderator", moderator);
        return map;
    }

    @Override
    public boolean isNull() {
        return getUser().complete() == null || getModerator().complete() == null;
    }

    @NotNull
    public CacheRestAction<User> getUser() {
        return jda.retrieveUserById(user);
    }

    @NotNull
    public CacheRestAction<User> getModerator() {
        return jda.retrieveUserById(moderator);
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

    public void ban() {
        getUser().queue(userJda -> {
            for (final Guild guild : jda.getMutualGuilds(userJda)) {
                final Member member = guild.getMember(userJda);
                if (member != null && guild.getSelfMember().canInteract(member)) guild.ban(member, 1, TimeUnit.DAYS).reason(reason).queue(s -> {}, f -> {});
            }
        });
    }

    public void unban() {
        getUser().queue(userJda -> {
            for (final Guild guild : jda.getGuilds()) guild.unban(userJda).queue();
        });
    }
}
