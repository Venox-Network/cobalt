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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReactChannel {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_EMOJIS = "emojis";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_EMOJIS) @Nullable public List<String> emojis;

    @Nullable
    public List<EmojiUnion> emojis() {
        if (emojis == null) return null;
        final List<EmojiUnion> emojiUnions = new ArrayList<>();
        for (final String emoji : emojis) emojiUnions.add(Emoji.fromFormatted(emoji));
        return emojiUnions;
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
        final Matcher matcher = Pattern.compile("(<a?:\\w+:\\d+>)|(:\\w+:)").matcher(message.getContentRaw());
        while (matcher.find()) {
            final String emojiString = matcher.group();
            if (emojiString == null || emojiString.isEmpty()) continue;
            emojis.add(emojiString);
            message.addReaction(Emoji.fromFormatted(emojiString)).queue();
        }
        return emojis;
    }
}
