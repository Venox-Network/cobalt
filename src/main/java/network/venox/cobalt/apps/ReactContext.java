package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.Message;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class ReactContext extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Dynamic react")
    public void reactContext(@NotNull GlobalMessageEvent event) {
        if (!cobalt.config.checkIsOwner(event)) return;
        final Message message = event.getTarget();
        CoUtilities.dynamicReact(message);
        event.reply("Dynamically reacted to " + message.getJumpUrl() + " with these emojis:\n").setEphemeral(true).queue();
    }
}
