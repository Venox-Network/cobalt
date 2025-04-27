package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.components.TranslateMenu;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class TranslateCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "translate",
            description = "Translate a message to another language")
    public void translateCommand(@NotNull GlobalSlashEvent event,
                                 @AppOption(description = "The message to translate") @NotNull String message) {
        final TranslateMenu.TranslateMessage translateMessage = TranslateMenu.getMessage(message, bot.oldData.getUser(event.getUser()).language);
        event.reply(translateMessage.message()).setComponents(translateMessage.actionRow()).setEphemeral(true).queue();
    }
}
