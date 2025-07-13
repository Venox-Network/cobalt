package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class ReactChannel {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_EMOJIS = "emojis";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_EMOJIS) @Nullable public List<String> emojis;

    @Nullable
    public List<EmojiUnion> emojis() {
        return emojis == null ? null : emojis.stream()
                .map(Emoji::fromFormatted)
                .toList();
    }

    public void addReactions(@NotNull Message message) {
        final List<EmojiUnion> emojiUnions = emojis();

        // Dynamic
        if (emojiUnions == null) {
            dynamicReact(message);
            return;
        }

        // Static
        emojiUnions.forEach(emoji -> message.addReaction(emoji).queue());
    }

    @NotNull
    public static List<String> dynamicReact(@NotNull Message message) {
        final List<String> emojis = new ArrayList<>();
        for (final String emojiString : message.getContentRaw().split("(?<=^|\\s)(<a?:\\w+:\\d+>|:\\w+:)(?=\\s|$)")) {
            if (emojiString.isEmpty()) continue;
            emojis.add(emojiString);
            message.addReaction(Emoji.fromFormatted(emojiString)).queue();
        }
        return emojis;
    }
}
