package network.venox.cobalt.commands.subcommands.qotdcmd;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.QotdCmd;
import network.venox.cobalt.data.objects.CoQuestion;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ListCmd extends CoSubCommand {
    public ListCmd(@NotNull QotdCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String name() {
        return "list";
    }

    @Override @NotNull
    public String description() {
        return "List all (or a specific) QOTD questions in the database";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "user", "The user ID of who created the question", false, true),
                new OptionData(OptionType.STRING, "id", "The ID of the question to get", false, true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        // Defer reply
        event.deferReply(true).queue();

        // ID-specific
        final OptionMapping idOption = event.getOption("id");
        if (idOption != null) {
            final int id = idOption.getAsInt();
            final CoQuestion question = cobalt.data.global.getQuestion(id);
            if (question == null) {
                event.getHook().editOriginal("Question with ID `" + id + "` does not exist!").queue();
                return;
            }
            event.getHook().editOriginal("**" + question.id + ":** " + question.question).queue();
            return;
        }

        // User-specific
        List<CoQuestion> questions = cobalt.data.global.qotds;
        final OptionMapping userOption = event.getOption("user");
        if (userOption != null) {
            final long userId = userOption.getAsLong();
            questions = questions.stream()
                    .filter(question -> question.user == userId)
                    .toList();
            if (questions.isEmpty()) {
                event.getHook().editOriginal(event.getJDA().retrieveUserById(userId).complete().getAsMention() + " has no questions!").queue();
                return;
            }
        }

        // Send questions
        event.getHook().sendMessage(questions.stream()
                        .map(question -> "**" + question.id + ":** " + question.question)
                        .collect(Collectors.joining("\n")))
                .queue();
    }

    @Override @Nullable
    public List<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        final String focusedOption = event.getFocusedOption().getName();

        // user
        if (focusedOption.equals("user")) {
            return cobalt.data.global.qotds.stream()
                    .map(question -> {
                        final User user = question.getUser(event.getJDA());
                        if (user == null) return null;
                        return new Command.Choice(user.getAsTag(), user.getIdLong());
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        // id
        if (focusedOption.equals("id")) {
            Stream<CoQuestion> questions = cobalt.data.global.qotds.stream();
            final OptionMapping userOption = event.getOption("user");
            if (userOption != null) {
                final long userId = userOption.getAsLong();
                questions = questions
                        .filter(question -> question.user == userId);
            }
            return questions
                    .map(question -> new Command.Choice(String.valueOf(question.id), question.id))
                    .toList();
        }

        return null;
    }
}
