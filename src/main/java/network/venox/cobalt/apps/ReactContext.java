package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.Message;

import network.venox.cobalt.Cobalt;

import network.venox.cobalt.mongo.ReactChannel;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@CommandMarker
public class ReactContext extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Dynamic react")
    public void reactContext(@NotNull GlobalMessageEvent event) {
        if (!bot.config.checkIsOwner(event)) return;
        final Message message = event.getTarget();
        final List<String> emojis = ReactChannel.dynamicReact(message);
        event.reply(LazyEmoji.YES + " Dynamically reacted to " + message.getJumpUrl() + " with these emojis:\n" + emojis.stream()
                        .reduce((a, b) -> a + " " + b)
                        .orElse("None"))
                .setEphemeral(true).queue();
    }
}
