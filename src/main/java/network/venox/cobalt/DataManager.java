package network.venox.cobalt;

import net.dv8tion.jda.api.entities.Guild;

import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.util.HashMap;
import java.util.Map;


public class DataManager {
    @NotNull public final MagicDatabase mongo;
    @NotNull public final Map<Long, GuildStats> guildStats = new HashMap<>();

    public DataManager(@NotNull Cobalt bot) {
        final String url = bot.settings.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        mongo = new SingleMongo(url).database.loadMagicCollections(Map.of("servers", Server.class));

        // Get guild stats
        for (final Guild guild : bot.jda.getGuilds()) guild.loadMembers().onSuccess(members -> {
            final int memberCount = members.size();
            final int humanCount = (int) members.stream()
                    .filter(member -> !member.getUser().isBot())
                    .count();
            guildStats.put(guild.getIdLong(), new GuildStats(memberCount, humanCount));
        });
    }

    public record GuildStats(int memberCount, int humanCount) {}
}
