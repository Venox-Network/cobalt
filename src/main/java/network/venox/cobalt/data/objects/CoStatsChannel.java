package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.Function;


public class CoStatsChannel implements CoObject {
    @NotNull private final Cobalt cobalt;

    public final long id;
    @NotNull public final String text;
    @NotNull
    public final CoStatsType type;

    public CoStatsChannel(@NotNull Cobalt cobalt, long id, @NotNull String text, @NotNull CoStatsType type) {
        this.cobalt = cobalt;
        this.id = id;
        this.text = text;
        this.type = type;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "text", text,
                "type", type.name());
    }

    public VoiceChannel getChannel(@NotNull Guild guild) {
        return guild.getVoiceChannelById(id);
    }

    @Nullable
    public VoiceChannelManager update(@NotNull Guild guild) {
        final VoiceChannel channel = getChannel(guild);

        // Channel was deleted
        if (channel == null) {
            cobalt.data.getGuild(guild).statsChannels.remove(this);
            return null;
        }

        // Edit channel name
        return channel.getManager().setName(text.replace("%count%", new DecimalFormat("#,###").format(type.update.apply(guild))));
    }

    public enum CoStatsType {
        MEMBERS(guild -> guild.loadMembers().get().size()),
        HUMANS(guild -> (int) guild.loadMembers().get().stream()
                .filter(member -> !member.getUser().isBot())
                .count()),
        /**
         * Not implemented yet
         */
        YOUTUBE_SUBSCRIBERS,
        /**
         * Not implemented yet
         */
        TWITCH_FOLLOWERS,
        /**
         * Not implemented yet
         */
        TIKTOK_FOLLOWERS,
        /**
         * Not implemented yet
         */
        TWITTER_FOLLOWERS,
        /**
         * Not implemented yet
         */
        INSTAGRAM_FOLLOWERS;

        @NotNull public final Function<Guild, Integer> update;

        CoStatsType(@NotNull Function<Guild, Integer> update) {
            this.update = update;
        }

        CoStatsType() {
            this.update = guild -> 0;
        }

        @Nullable
        public static CoStatsType getType(@Nullable String name) {
            try {
                return CoStatsType.valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
