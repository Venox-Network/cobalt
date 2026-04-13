package network.venox.cobalt.apps.message;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.Message;

import network.venox.cobalt.CoConfig;
import network.venox.cobalt.mongo.ReactChannel;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import java.util.List;


@Command
public class DynamicReact {
    @NotNull private final CoConfig config;

    public DynamicReact(@NotNull CoConfig config) {
        this.config = config;
    }

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Dynamic react")
    public void dynamicReact(@NotNull GlobalMessageEvent event) {
        if (!config.checkIsOwner(event)) return;
        final Message message = event.getTarget();
        final List<String> emojis = ReactChannel.dynamicReact(message);
        event.reply(LazyEmoji.YES + " Dynamically reacted to " + message.getJumpUrl() + " with these emojis:\n" + emojis.stream()
                        .reduce((a, b) -> a + " " + b)
                        .orElse("None"))
                .setEphemeral(true).queue();
    }
}
