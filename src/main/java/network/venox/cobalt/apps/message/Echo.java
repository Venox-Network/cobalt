package network.venox.cobalt.apps.message;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;


@Command
public class Echo {
    @NotNull private final CoConfig config;

    public Echo(@NotNull CoConfig config) {
        this.config = config;
    }

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Echo")
    public void echoContext(@NotNull GlobalMessageEvent event) {
        if (!config.checkIsOwner(event)) return;
        // Delete reply
        event.deferReply(true)
                .flatMap(InteractionHook::deleteOriginal)
                .queue();
        // Send message
        final MessageChannelUnion channel = event.getChannel();
        if (channel != null) channel.sendMessage(event.getTarget().getContentRaw()).queue();
    }
}
