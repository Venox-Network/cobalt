package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.components.TranslateMenu;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import space.dynomake.libretranslate.Language;

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
        // Get language
        final Language languageEnum;
        try {
            languageEnum = Language.valueOf(language.toUpperCase());
        } catch (final IllegalArgumentException e) {
            event.reply(LazyEmoji.NO + " Invalid language: `" + language + "`!").setEphemeral(true).queue();
            return;
        }

        // Update
        final CoUser previousUser = bot.dataManager.mongo.getMagicCollection(CoUser.class).findOneAndUpdate(
                Filters.eq("_id", event.getUser().getIdLong()),
                Updates.set("language", languageEnum),
                new FindOneAndUpdateOptions().upsert(true));

        // Reply
        final StringBuilder reply = new StringBuilder(LazyEmoji.YES + " Your primary language is now `" + languageEnum + "`");
        if (previousUser != null) {
            final Language previous = previousUser.language;
            if (previous != null) reply.append(" (previous: `").append(previous).append("`)");
        }
        event.reply(reply.toString()).setEphemeral(true).queue();
    }

    @Override @NotNull
    public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
        return LANGUAGE_CHOICES;
    }
}
