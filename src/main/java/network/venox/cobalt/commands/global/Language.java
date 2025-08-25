package network.venox.cobalt.commands.global;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@io.github.freya022.botcommands.api.commands.annotations.Command
public class Language extends ApplicationCommand {
    @NotNull private static final List<Command.Choice> LANGUAGE_CHOICES = Translate.LANGUAGES.stream()
            .map(language -> {
                final String name = language.name();
                return new Command.Choice(name.charAt(0) + name.substring(1).toLowerCase(), name);
            })
            .toList();

    @NotNull private final MongoProvider mongo;

    public Language(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "language",
            description = "Set your primary language (for /translate)")
    public void language(@NotNull GlobalSlashEvent event,
                         @SlashOption(description = "Your new primary language") @NotNull String language) {
        // Get language
        final space.dynomake.libretranslate.Language languageEnum;
        try {
            languageEnum = space.dynomake.libretranslate.Language.valueOf(language.toUpperCase());
        } catch (final IllegalArgumentException e) {
            event.reply(LazyEmoji.NO + " Invalid language: `" + language + "`!").setEphemeral(true).queue();
            return;
        }

        // Update
        final CoUser previousUser = mongo.database.getMagicCollection(CoUser.class).findOneAndUpdate(
                Filters.eq("_id", event.getUser().getIdLong()),
                Updates.set("language", languageEnum),
                new FindOneAndUpdateOptions().upsert(true));

        // Reply
        final StringBuilder reply = new StringBuilder(LazyEmoji.YES + " Your primary language is now `" + languageEnum + "`");
        if (previousUser != null) {
            final space.dynomake.libretranslate.Language previous = previousUser.language;
            if (previous != null) reply.append(" (previous: `").append(previous).append("`)");
        }
        event.reply(reply.toString()).setEphemeral(true).queue();
    }

    @Override @NotNull
    public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String optionName) {
        return LANGUAGE_CHOICES;
    }
}
