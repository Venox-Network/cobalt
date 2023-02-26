package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public record CoSuperBan(@NotNull JDA jda, long user, @NotNull String reason, @Nullable Long time, long moderator) implements CoObject {
    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("reason", reason);
        map.put("time", time);
        map.put("moderator", moderator);
        return map;
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
        if (builder.length() == 0) builder.append("0s");
        return builder.toString();
    }

    public void ban() {
        getUser().queue(user -> {
            for (final Guild guild : jda.getMutualGuilds(user)) {
                final Member member = guild.getMember(user);
                if (member != null && guild.getSelfMember().canInteract(member)) guild.ban(member, 1, TimeUnit.DAYS).reason(reason).queue(s -> {}, f -> {});
            }
        }, f -> {});
    }

    public void unban() {
        getUser().queue(user -> {
            for (final Guild guild : jda.getGuilds()) guild.unban(user).queue();
        }, f -> {});
    }
}
