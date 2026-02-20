package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;

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
    @BsonProperty(PROP_IGNORED_PHRASES) @Nullable public Set<String> ignoredPhrases;
    @BsonProperty(PROP_IGNORED_ROLES) @Nullable public Set<Long> ignoredRoles;

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

    @NotNull
    public String name(@NotNull Message message) {
        String threadName = name;
        if (threadName == null || threadName.isBlank()) threadName = "%message%";

        // Placeholders
        threadName = threadName
                .replace("%count%", String.valueOf(count))
                .replace("%message%", message.getContentRaw());

        if (threadName.isBlank()) {
            // Try to get from first embed title
            final List<MessageEmbed> embeds = message.getEmbeds();
            if (!embeds.isEmpty()) {
                final String title = embeds.getFirst().getTitle();
                if (title != null) threadName = title;
            }

            // Fallback to generic name
            if (threadName.isBlank()) threadName = "Thread " + count;
        }

        // Shorten
        return StringUtility.shorten(threadName, 100);
    }

    @NotNull
    public Set<String> ignoredPhrases() {
        return Objects.requireNonNullElseGet(ignoredPhrases, HashSet::new);
    }

    @NotNull
    public Set<Long> ignoredRoles() {
        return Objects.requireNonNullElseGet(ignoredRoles, HashSet::new);
    }

    @BService
    public static class Manager {
        @NotNull private final MongoProvider mongo;

        public Manager(@NotNull MongoProvider mongo) {
            this.mongo = mongo;
        }

        public void createThread(@NotNull AutoThread thread, @NotNull Message message) {
            // Check ignoredPhrases
            final String content = message.getContentRaw().toLowerCase().trim();
            for (final String ignoredPhrase : thread.ignoredPhrases()) if (content.contains(ignoredPhrase)) return;

            // Check ignoredRoles
            final Set<Long> ignoredRolesNonNull = thread.ignoredRoles();
            if (!ignoredRolesNonNull.isEmpty()) {
                final Member member = message.getMember();
                if (member == null) return;

                // Get member roles IDs
                final Set<Long> memberRoles = new HashSet<>();
                for (final Role role : member.getRoles()) memberRoles.add(role.getIdLong());

                // Check for intersection
                if (!Collections.disjoint(ignoredRolesNonNull, memberRoles)) return;
            }

            // Create thread
            message.createThreadChannel(thread.name(message)).queue();
            mongo.database.getMagicCollection(AutoThread.class).updateOne(
                    Filters.eq("_id", thread.channel),
                    Updates.inc(PROP_COUNT, 1));
        }
    }
}
