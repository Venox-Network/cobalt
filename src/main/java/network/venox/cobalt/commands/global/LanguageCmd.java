package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import net.suuft.libretranslate.Language;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.components.TranslateMenu;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@CommandMarker
public class LanguageCmd extends ApplicationCommand {
    @NotNull private static final List<Command.Choice> LANGUAGE_CHOICES = TranslateMenu.LANGUAGES.stream()
            .map(language -> {
                final String name = language.name();
                return new Command.Choice(name.charAt(0) + name.substring(1).toLowerCase(), name);
            })
            .toList();

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "language",
            description = "Set your primary language (for /translate)")
    public void language(@NotNull GlobalSlashEvent event,
                         @AppOption(description = "Your new primary language") @NotNull String language) {
        final Language languageEnum;
        try {
            languageEnum = Language.valueOf(language.toUpperCase());
        } catch (final IllegalArgumentException e) {
            event.reply(LazyEmoji.NO + " Invalid language: `" + language + "`!").setEphemeral(true).queue();
            return;
        }

        final CoUser user = bot.oldData.getUser(event.getUser());
        event.reply(LazyEmoji.YES + " Your primary language is now `" + languageEnum + "`, previous: `" + user.language + "`").setEphemeral(true).queue();
        user.language = languageEnum;
    }

    @Override @NotNull
    public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
        return LANGUAGE_CHOICES;
    }
}
