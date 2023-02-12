package network.venox.cobalt.commands.subcommands.highlightcmd;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class RemoveCmd extends CoSubCommand {
    public RemoveCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Remove an existing highlight";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "word", "The word to remove from your highlights", true, true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping wordOption = event.getOption("word");
        if (wordOption == null) return;
        final String word = wordOption.getAsString().toLowerCase();
        event.getCoUser().highlights.remove(word);
        event.reply("Removed `" + word + "` from your highlights").setEphemeral(true).queue();
    }

    @Override @NotNull
    public Set<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        return event.getCoUser().highlights.stream()
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toSet());
    }
}
