package network.venox.cobalt;

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmoji;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer;

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;


@AppEmojiContainer
public class CoEmoji {
    @AppEmoji
    public static final ApplicationEmoji PLUS = AppEmojisRegistry.get("PLUS");
}
