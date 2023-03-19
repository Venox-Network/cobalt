package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class EchoContext extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Echo")
    public void echoContext(@NotNull GlobalMessageEvent event) {
        if (!cobalt.config.checkIsOwner(event)) return;
        // Delete reply
        event.deferReply(true)
                .flatMap(InteractionHook::deleteOriginal)
                .queue();
        // Send message
        final MessageChannelUnion channel = event.getChannel();
        if (channel != null) channel.sendMessage(event.getTarget().getContentRaw()).queue();
    }
}
