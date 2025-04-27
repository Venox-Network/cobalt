package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;

import java.util.*;
import java.util.stream.Collectors;


public final class CoThreadChannel extends CoObject {
    @NotNull private final JDA jda;
    private final long guildId;

    public final long channel;
    @Nullable public final String name;
    public int count;
    @NotNull public final Set<String> ignoredPhrases;
    @NotNull public final Set<Long> ignoredRoles;

    public CoThreadChannel(@NotNull JDA jda, long guildId, long channel, @Nullable String name, int count, @Nullable Set<String> ignoredPhrases, @Nullable Set<Long> ignoredRoles) {
        this.jda = jda;
        this.guildId = guildId;
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
        if (!ignoredPhrases.isEmpty()) map.put("ignoredPhrases", new ArrayList<>(ignoredPhrases));
        if (!ignoredRoles.isEmpty()) map.put("ignoredRoles", new ArrayList<>(ignoredRoles));
        return map;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public StandardGuildMessageChannel getChannel() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getChannelById(StandardGuildMessageChannel.class, channel);
    }

    @Nullable
    public Set<Role> getIgnoredRoles() {
        final Guild guild = getGuild();
        if (guild == null) return null;
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

        // Shorten
        return StringUtility.shorten(threadName, 100);
    }

    public void createThread(@NotNull Message message) {
        // Check ignoredPhrases
        final String content = message.getContentRaw().toLowerCase().trim();
        for (final String ignoredPhrase : ignoredPhrases) if (content.contains(ignoredPhrase)) return;

        // Check ignoredRoles
        if (!ignoredRoles.isEmpty()) {
            final Member member = message.getMember();
            final Set<Role> ignoredRolesSet = getIgnoredRoles();
            if (member == null || ignoredRolesSet == null || !Collections.disjoint(ignoredRolesSet, member.getRoles())) return;
        }

        // Create thread
        message.createThreadChannel(getName(message)).queue();
        count++;
    }
}
