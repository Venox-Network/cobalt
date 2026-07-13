package network.venox.cobalt.emoji;

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmoji;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import xyz.srnyx.lazylibrary.emoji.ApplicationEmojiWrapper;


@AppEmojiContainer
public class CoEmojiRegistry {
    @AppEmoji
    public static final ApplicationEmoji PLUS = AppEmojisRegistry.get("PLUS");
}
