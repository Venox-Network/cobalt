package network.venox.cobalt.commands.subcommands.qotdcmd;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.QotdCmd;
import network.venox.cobalt.data.objects.CoQuestion;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public class RemoveCmd extends CoSubCommand {
    public RemoveCmd(@NotNull QotdCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Remove a QOTD from the database";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "ids", "The ID(s) of the question(s) to remove (use ',' to separate for multiple)", true, true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        // Options
        final OptionMapping idsOption = event.getOption("ids");
        if (idsOption == null) return;
        final String idsString = idsOption.getAsString();

        // Defer reply
        event.deferReply(true).queue();

        // Get IDs
        final List<Integer> ids = Arrays.stream(idsString.split(","))
                .map(CoMapper::toInt)
                .filter(Objects::nonNull)
                .toList();

        // Remove questions
        final List<CoQuestion> removedQuestions = new ArrayList<>();
        for (final int id : ids) {
            final CoQuestion question = cobalt.data.global.getQuestion(id);
            if (question == null) continue;
            cobalt.data.global.qotds.remove(question);
            removedQuestions.add(question);
        }

        // Reply
        event.getHook().editOriginal("Removed " + removedQuestions.size() + " question(s)\n" + removedQuestions.stream()
                        .map(question -> "**" + question.id + ":** " + question.question)
                        .collect(Collectors.joining("\n")))
                .queue();
    }

    @Override @Nullable
    public List<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        final OptionMapping idsOption = event.getOption("ids");
        if (idsOption == null) return null;
        return cobalt.data.global.qotds.stream()
                .map(question -> new Command.Choice(String.valueOf(question.id), question.id))
                .toList();
    }
}
