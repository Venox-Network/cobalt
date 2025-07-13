package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@CommandMarker
public class HighlightCmd extends ApplicationCommand {
    @NotNull private static final String AC_REMOVE_WORDS = "HighlightCmd.removeCommand.word";

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "list",
            description = "List all of your existing highlights")
    public void listCommand(@NotNull GlobalSlashEvent event) {
        final Set<String> highlights = bot.dataManager.mongo.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());
        if (highlights.isEmpty()) {
            event.reply(LazyEmoji.NO + " You don't have any highlights!").setEphemeral(true).queue();
            return;
        }
        event.reply(LazyEmoji.YES + " `" + String.join("`, `", highlights) + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "add",
            description = "Add a new highlight")
    public void addCommand(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The word(s) to highlight. Use spaces to separate multiple") @NotNull String words) {
        final MagicCollection<CoUser> collection = bot.dataManager.mongo.getMagicCollection(CoUser.class);
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

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "remove",
            description = "Remove (an) existing highlight(s)")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                              @AppOption(description = "The word to remove from your highlights. Use spaces to separate multiple", autocomplete = AC_REMOVE_WORDS) @NotNull String words) {
        final MagicCollection<CoUser> collection = bot.dataManager.mongo.getMagicCollection(CoUser.class);
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

    @AutocompletionHandler(name = AC_REMOVE_WORDS) @NotNull
    public Set<String> removeAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        return bot.dataManager.mongo.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());
    }
}
