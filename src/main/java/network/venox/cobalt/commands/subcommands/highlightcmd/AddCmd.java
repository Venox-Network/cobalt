package network.venox.cobalt.commands.subcommands.highlightcmd;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class AddCmd extends CoSubCommand {
    public AddCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Add a new highlight";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "words", "The word(s) to highlight. Use spaces to separate multiple", true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping wordsOption = event.getOption("words");
        if (wordsOption == null) return;
        final Set<String> words = Arrays.stream(wordsOption.getAsString().split(" "))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        event.getCoUser().highlights.addAll(words);
        event.reply("Added `" + String.join("`, `", words) + "` to your highlights").setEphemeral(true).queue();
    }
}
