package network.venox.cobalt.commands.subcommands.qotdcmd;

import info.debatty.java.stringsimilarity.JaroWinkler;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.QotdCmd;
import network.venox.cobalt.data.objects.CoQuestion;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class AddCmd extends CoSubCommand {
    public AddCmd(@NotNull QotdCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Add a QOTD to the database";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Arrays.asList(
                new OptionData(OptionType.STRING, "questions", "The questions to add", true),
                new OptionData(OptionType.STRING, "delimiter", "The characters used to separate multiple questions", false));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        // Options
        final OptionMapping questionsOption = event.getOption("questions");
        if (questionsOption == null) return;
        final OptionMapping delimiterOption = event.getOption("delimiter");
        final String questionString = questionsOption.getAsString();

        // Get questions
        List<String> questionList = Collections.singletonList(questionString);
        if (delimiterOption != null) questionList = Arrays.asList(questionString.split(Pattern.quote(delimiterOption.getAsString())));

        // Defer reply
        event.deferReply(true).queue();

        // Check question similarity
        final List<String> questionListLower = questionList.stream()
                .map(question -> question.toLowerCase().trim())
                .toList();
        final JaroWinkler jaroWinkler = new JaroWinkler();
        for (final CoQuestion qotd : cobalt.data.global.qotds) {
            final String qotdLower = qotd.question.toLowerCase().trim();
            final String match = questionListLower.stream()
                    .filter(question -> jaroWinkler.similarity(question, qotdLower) < 0.5)
                    .findFirst()
                    .orElse(null);
            if (match == null) continue;
            event.getHook().editOriginalEmbeds(cobalt.messages.getEmbed("command", "qotd", "add", "similar")
                    .replace("%question%", match)
                    .replace("%matchid%", qotd.id)
                    .replace("%matchquestion%", qotd.question)
                    .build()
            ).queue();
            return;
        }

        // Add to database
        final List<CoQuestion> coQuestions = new ArrayList<>();
        for (final String question : questionList) {
            final int id = cobalt.data.global.getNextQotdId();
            final CoQuestion coQuestion = new CoQuestion(id, question, event.getUser().getIdLong(), 0);
            cobalt.data.global.qotds.add(coQuestion);
            coQuestions.add(coQuestion);
        }

        // Reply
        event.getHook().editOriginalEmbeds(cobalt.messages.getEmbed("command", "qotd", "add", "success")
                .replace("%questions%", coQuestions.stream()
                        .map(question -> "**" + question.id + ":** " + question.question)
                        .collect(Collectors.joining("\n")))
                .build()
        ).queue();
    }
}
