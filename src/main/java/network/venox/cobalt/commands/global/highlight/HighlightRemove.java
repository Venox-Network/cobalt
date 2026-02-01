package network.venox.cobalt.commands.global.highlight;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Command
public class HighlightRemove {
    @NotNull private static final String AC_REMOVE_WORDS = "HighlightCmd.removeCommand.word";

    @NotNull private final MongoProvider mongo;

    public HighlightRemove(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "highlight",
            subcommand = "remove",
            description = "Remove (an) existing highlight(s)")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                              @SlashOption(description = "The word to remove from your highlights. Use spaces to separate multiple", autocomplete = AC_REMOVE_WORDS) @NotNull String words) {
        final MagicCollection<CoUser> collection = mongo.database.getMagicCollection(CoUser.class);
        final Set<String> highlights = collection
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());

        // Get words
        final Set<String> wordSet = Arrays.stream(words.split(" "))
                .map(String::toLowerCase)
                .filter(highlights::contains)
                .collect(Collectors.toSet());
        if (wordSet.isEmpty()) {
            event.reply(LazyEmoji.NO + " You don't have any of those highlights!").setEphemeral(true).queue();
            return;
        }

        // Remove words from highlights
        collection.updateOne(
                Filters.eq("_id", event.getUser().getIdLong()),
                Updates.pullAll(CoUser.PROP_HIGHLIGHTS, new ArrayList<>(wordSet)));
        event.reply(LazyEmoji.YES + " Removed `" + String.join("`, `", wordSet) + "` from your highlights").setEphemeral(true).queue();
    }

    @AutocompleteHandler(AC_REMOVE_WORDS) @NotNull
    public Set<String> removeAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        return mongo.database.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());
    }
}
