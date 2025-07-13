package network.venox.cobalt;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;


public class DataManager {
    @NotNull public final MagicDatabase mongo;
    /**
     * Total member count across all guilds
     */
    public int totalMembers = 0;
    /**
     * Total unique members across all guilds
     */
    public final int uniqueMembers;
    /**
     * [channel ID, future]
     */
    @NotNull public final Map<Long, ScheduledFuture<?>> stickyFutures = new HashMap<>();
    /**
     * [user ID, [guild ID, next highlight time]]
     */
    @NotNull public final Map<Long, Map<Long, Long>> highlightCooldowns = new HashMap<>();

    public DataManager(@NotNull Cobalt bot) {
        final String url = bot.settings.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        mongo = new SingleMongo(url).database.loadMagicCollections(Map.of("servers", Server.class));

        // guildCount, totalMembers, uniqueMembers
        final Set<Long> users = new HashSet<>();
        for (final Guild guild : bot.jda.getGuilds()) guild.loadMembers().onSuccess(members -> {
            for (final Member member : members) {
                totalMembers++;
                users.add(member.getIdLong());
            }
        });
        uniqueMembers = users.size();
    }
}
