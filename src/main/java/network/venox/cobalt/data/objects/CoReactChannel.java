package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import network.venox.cobalt.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoReactChannel extends CoObject {
    @NotNull private final JDA jda;
    private final long guildId;

    public final long channel;
    @Nullable public List<String> emojis;

    public CoReactChannel(@NotNull JDA jda, long guildId, long channel, @Nullable List<String> emojis) {
        this.jda = jda;
        this.guildId = guildId;
        this.channel = channel;
        this.emojis = emojis;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        if (emojis != null) map.put("emojis", emojis);
        return map;
    }

    @Override
    public boolean isNull() {
        return getChannel() == null;
    }

    @Nullable
    public List<EmojiUnion> getEmojis() {
        if (emojis == null) return null;
        return emojis.stream()
                .map(Emoji::fromFormatted)
                .toList();
    }

    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public TextChannel getChannel() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(channel);
    }

    public void addReactions(@NotNull Message message) {
        final List<EmojiUnion> emojiUnions = getEmojis();
        // Dynamic
        if (emojiUnions == null) {
            CoUtilities.dynamicReact(message);
            return;
        }
        // Static
        emojiUnions.forEach(emoji -> message.addReaction(emoji).queue());
    }
}
