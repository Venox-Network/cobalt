package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.components.TranslateMenu;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import space.dynomake.libretranslate.Language;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class TranslateContext extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Translate")
    public void translateContext(@NotNull GlobalMessageEvent event) {
        final String message = event.getTarget().getContentStripped();
        if (message.isEmpty()) {
            event.reply(LazyEmoji.NO + " Message cannot be empty!").setEphemeral(true).queue();
            return;
        }
        event.deferReply(true).queue();

        // Get user's language
        final Language language = bot.dataManager.mongo.getMagicCollection(CoUser.class).findOne("_id", event.getUser().getIdLong())
                .map(coUser -> coUser.language)
                .orElse(Language.ENGLISH);

        // Translate and reply
        event.getHook().editOriginal(TranslateMenu.getMessage(message, language)).queue();
    }
}
