package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;

import java.util.*;


public class AutoThread {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_NAME = "name";
    @NotNull public static final String PROP_COUNT = "count";
    @NotNull public static final String PROP_IGNORED_PHRASES = "ignored_phrases";
    @NotNull public static final String PROP_IGNORED_ROLES = "ignored_roles";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_NAME) @Nullable public String name;
    @BsonProperty(PROP_COUNT) public int count;
    @BsonProperty(PROP_IGNORED_PHRASES) public Set<String> ignoredPhrases;
    @BsonProperty(PROP_IGNORED_ROLES) public Set<Long> ignoredRoles;

    public AutoThread() {}

    public AutoThread(@NotNull GuildChannel channel, @Nullable String name) {
        this.guild = channel.getGuild().getIdLong();
        this.channel = channel.getIdLong();
        this.name = name;
        this.count = 1;
    }

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @Nullable
    public Set<Role> ignoredRoles(@NotNull JDA jda) {
        final Guild guild = guild(jda).orElse(null);
        if (guild == null) return null;
        final Set<Role> roles = new HashSet<>();
        for (long ignoredRole : ignoredRoles) {
            final Role role = guild.getRoleById(ignoredRole);
            if (role != null) roles.add(role);
        }
        return roles;
    }

    @NotNull
    public String name(@NotNull Message message) {
        String threadName = name;
        if (threadName == null || threadName.isBlank()) threadName = "%message%";

        // Placeholders
        threadName = threadName
                .replace("%count%", String.valueOf(count))
                .replace("%message%", message.getContentRaw());

        // Shorten
        return StringUtility.shorten(threadName, 100);
    }

    public void createThread(@NotNull Cobalt bot, @NotNull Message message) {
        // Check ignoredPhrases
        final String content = message.getContentRaw().toLowerCase().trim();
        for (final String ignoredPhrase : ignoredPhrases) if (content.contains(ignoredPhrase)) return;

        // Check ignoredRoles
        if (!ignoredRoles.isEmpty()) {
            final Member member = message.getMember();
            final Set<Role> ignoredRolesSet = ignoredRoles(bot.jda);
            if (member == null || ignoredRolesSet == null || !Collections.disjoint(ignoredRolesSet, member.getRoles())) return;
        }

        // Create thread
        message.createThreadChannel(name(message)).queue();
        bot.dataManager.mongo.getMagicCollection(AutoThread.class).updateOne(
                Filters.eq("_id", channel),
                Updates.inc(PROP_COUNT, 1));
    }
}
