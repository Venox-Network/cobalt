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
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import space.dynomake.libretranslate.Language;


@CommandMarker
public class TranslateCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "translate",
            description = "Translate a message to another language")
    public void translateCommand(@NotNull GlobalSlashEvent event,
                                 @AppOption(description = "The message to translate") @NotNull String message) {
        event.deferReply(true).queue();
        event.getHook().editOriginal(TranslateMenu.getMessage(message, bot.dataManager.mongo.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.language)
                .orElse(Language.ENGLISH))).queue();
    }
}
