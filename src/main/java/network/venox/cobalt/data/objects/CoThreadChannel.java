package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public final class CoThreadChannel implements CoObject {
    public final long channel;
    @Nullable public final String name;
    public int count;
    @NotNull public final Set<String> ignoredPhrases;
    @NotNull public final Set<Long> ignoredRoles;

    public CoThreadChannel(long channel, @Nullable String name, int count, @Nullable Set<String> ignoredPhrases, @Nullable Set<Long> ignoredRoles) {
        this.channel = channel;
        this.name = name;
        this.count = count;
        this.ignoredPhrases = ignoredPhrases == null ? Set.of() : ignoredPhrases;
        this.ignoredRoles = ignoredRoles == null ? Set.of() : ignoredRoles;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("count", count);
        map.put("ignoredPhrases", new ArrayList<>(ignoredPhrases));
        map.put("ignoredRoles", new ArrayList<>(ignoredRoles));
        return map;
    }

    @Nullable
    public StandardGuildMessageChannel getChannel(@NotNull Guild guild) {
        return guild.getChannelById(StandardGuildMessageChannel.class, channel);
    }

    @NotNull
    public Set<Role> getIgnoredRoles(@NotNull Guild guild) {
        return ignoredRoles.stream()
                .map(guild::getRoleById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @NotNull
    public String getName(@NotNull Message message) {
        String threadName = name;
        if (threadName == null || threadName.isBlank()) threadName = "%message%";

        // Placeholders
        threadName = threadName
                .replace("%count%", String.valueOf(count))
                .replace("%message%", message.getContentRaw());

        // Limit to 100 characters
        if (threadName.length() > 100) threadName = threadName.substring(0, 97) + "...";
        return threadName;
    }

    public void createThread(@NotNull Message message) {
        // Check ignoredPhrases
        final String content = message.getContentRaw().toLowerCase().trim();
        for (final String ignoredPhrase : ignoredPhrases) if (content.contains(ignoredPhrase)) return;

        // Check ignoredRoles
        if (!ignoredRoles.isEmpty()) {
            final Member member = message.getMember();
            if (member == null || !Collections.disjoint(getIgnoredRoles(message.getGuild()), member.getRoles())) return;
        }

        // Create thread
        message.createThreadChannel(getName(message)).queue();
        count++;
    }
}
