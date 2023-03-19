package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@CommandMarker
public class HighlightCmd extends ApplicationCommand {
    @NotNull private static final String AC_REMOVE_WORDS = "HighlightCmd.removeCommand.word";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "list",
            description = "List all of your existing highlights")
    public void listCommand(@NotNull GlobalSlashEvent event) {
        final Set<String> highlights = cobalt.data.getUser(event.getUser()).highlights;
        if (highlights.isEmpty()) {
            event.reply("You don't have any highlights!").setEphemeral(true).queue();
            return;
        }
        event.reply("`" + String.join("`, `", highlights) + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "add",
            description = "Add a new highlight")
    public void addCommand(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The word(s) to highlight. Use spaces to separate multiple") @NotNull String words) {
        final Set<String> highlights = cobalt.data.getUser(event.getUser()).highlights;
        final Set<String> wordSet = Arrays.stream(words.split(" "))
                .map(String::toLowerCase)
                .filter(word -> !highlights.contains(word))
                .collect(Collectors.toSet());
        if (wordSet.isEmpty()) {
            event.reply("You already have all of those highlights!").setEphemeral(true).queue();
            return;
        }
        highlights.addAll(wordSet);
        event.reply("Added `" + String.join("`, `", wordSet) + "` to your highlights").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "remove",
            description = "Remove (an) existing highlight(s)")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                              @AppOption(description = "The word to remove from your highlights. Use spaces to separate multiple", autocomplete = AC_REMOVE_WORDS) @NotNull String words) {
        final Set<String> highlights = cobalt.data.getUser(event.getUser()).highlights;
        final Set<String> wordSet = Arrays.stream(words.split(" "))
                .map(String::toLowerCase)
                .filter(highlights::contains)
                .collect(Collectors.toSet());
        if (wordSet.isEmpty()) {
            event.reply("You don't have any of those highlights!").setEphemeral(true).queue();
            return;
        }
        highlights.removeAll(wordSet);
        event.reply("Removed `" + String.join("`, `", wordSet) + "` from your highlights").setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_REMOVE_WORDS) @NotNull
    public Set<String> removeAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        return cobalt.data.getUser(event.getUser()).highlights;
    }
}
