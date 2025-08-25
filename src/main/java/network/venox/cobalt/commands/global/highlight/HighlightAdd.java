package network.venox.cobalt.commands.global.highlight;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

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
public class HighlightAdd extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public HighlightAdd(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "highlight",
            subcommand = "add",
            description = "Add a new highlight")
    public void addCommand(@NotNull GlobalSlashEvent event,
                           @SlashOption(description = "The word(s) to highlight. Use spaces to separate multiple") @NotNull String words) {
        final MagicCollection<CoUser> collection = mongo.database.getMagicCollection(CoUser.class);
        final Set<String> highlights = collection
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());

        // Get words
        final Set<String> wordSet = Arrays.stream(words.split(" "))
                .map(String::toLowerCase)
                .filter(word -> !highlights.contains(word))
                .collect(Collectors.toSet());
        if (wordSet.isEmpty()) {
            event.reply(LazyEmoji.NO + " You already have all of those highlights!").setEphemeral(true).queue();
            return;
        }

        // Check highlight count
        if (highlights.size() + wordSet.size() > 10) {
            event.reply(LazyEmoji.NO + " You can't have more than **10** highlights!").setEphemeral(true).queue();
            return;
        }

        // Add words to highlights
        collection.updateOne(
                Filters.eq("_id", event.getUser().getIdLong()),
                Updates.addEachToSet(CoUser.PROP_HIGHLIGHTS, new ArrayList<>(wordSet)));
        event.reply(LazyEmoji.YES + " Added `" + String.join("`, `", wordSet) + "` to your highlights").setEphemeral(true).queue();
    }
}
