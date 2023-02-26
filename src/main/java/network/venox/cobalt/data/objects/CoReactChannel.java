package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoReactChannel implements CoObject {
    public final long channel;
    @Nullable public List<String> emojis;

    public CoReactChannel(long channel, @Nullable List<String> emojis) {
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

    @Nullable
    public List<EmojiUnion> getEmojis() {
        if (emojis == null) return null;
        return emojis.stream()
                .map(Emoji::fromFormatted)
                .toList();
    }

    @Nullable
    public TextChannel getChannel(@NotNull Guild guild) {
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
