package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.components.Components;

import info.debatty.java.stringsimilarity.JaroWinkler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.data.objects.CoQuestion;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CommandMarker
public class QotdCmd extends ApplicationCommand {
    @NotNull private static final String AC_LIST_USER = "QotdCmd.listCommand.user";
    @NotNull private static final String AC_LIST_ID = "QotdCmd.listCommand.id";
    @NotNull private static final String AC_LIST_USES = "QotdCmd.listCommand.uses";
    @NotNull private static final String AC_REMOVE_IDS = "QotdCmd.removeCommand.ids";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotd",
            subcommand = "list",
            description = "List all (or a specific) QOTD questions in the database")
    public void listCommand(@NotNull GlobalSlashEvent event,
                            @AppOption(description = "The user ID of who created the question", autocomplete = AC_LIST_USER) @Nullable String user,
                            @AppOption(description = "The number of times the question has been used", autocomplete = AC_LIST_USES) @Nullable Integer uses,
                            @AppOption(description = "The ID of the question to get", autocomplete = AC_LIST_ID) @Nullable Integer id) {
        if (!cobalt.config.checkIsQotdManager(event)) return;

        // ID-specific
        if (id != null) {
            final CoQuestion question = cobalt.data.global.getQuestion(id);
            if (question == null) {
                event.reply("Question with ID `" + id + "` does not exist!").setEphemeral(true).queue();
                return;
            }
            event.reply(formatQuestion(question)).setEphemeral(true).queue();
            return;
        }

        // User-specific
        List<CoQuestion> questions = cobalt.data.global.qotds;
        if (user != null) {
            final Long userId = CoMapper.toLong(user);
            if (userId == null) {
                event.replyEmbeds(CoEmbed.invalidArgument(user).build()).setEphemeral(true).queue();
                return;
            }
            questions = questions.stream()
                    .filter(question -> question.user == userId)
                    .toList();
            if (questions.isEmpty()) {
                event.getJDA().retrieveUserById(userId)
                        .queue(userJda -> event.reply(userJda.getAsMention() + " has no questions!").setEphemeral(true).queue(),
                                throwable -> event.reply("User with ID `" + user + "` does not exist!").setEphemeral(true).queue());
                return;
            }
        }

        // Uses-specific
        if (uses != null) {
            questions = questions.stream()
                    .filter(question -> question.used <= uses)
                    .toList();
            if (questions.isEmpty()) {
                event.reply("No questions have `" + uses + "` or less uses!").setEphemeral(true).queue();
                return;
            }
        }

        // Build messages
        final List<String> messages = new ArrayList<>();
        final StringBuilder stringBuilder = new StringBuilder();
        for (final CoQuestion question : questions) {
            final String line = formatQuestion(question);
            if (stringBuilder.length() + line.length() > 2000) {
                messages.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
            stringBuilder.append(line).append("\n");
        }
        messages.add(stringBuilder.toString());
        final int size = messages.size();

        // Get buttons
        final AtomicInteger currentPage = new AtomicInteger();
        final Button previousButton = Components.primaryButton(buttonEvent -> {
            final int index = currentPage.get() - 1;
            // Check index
            if (index < 0) {
                buttonEvent.deferEdit().queue();
                return;
            }
            // Edit message
            buttonEvent.editMessage(messages.get(index)).queue();
            currentPage.set(index);
        }).build(Emoji.fromUnicode("U+2B05"));
        final Button nextButton = Components.primaryButton(buttonEvent -> {
            final int index = currentPage.get() + 1;
            // Check index
            if (index >= size) {
                buttonEvent.deferEdit().queue();
                return;
            }
            // Edit message
            buttonEvent.editMessage(messages.get(index)).queue();
            currentPage.set(index);
        }).build(Emoji.fromUnicode("U+27A1"));

        // Send initial message with buttons
        event.reply(messages.get(0)).setEphemeral(true).addActionRow(previousButton, nextButton).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotd",
            subcommand = "add",
            description = "Add a QOTD to the database")
    public void addCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The questions to add") @NotNull String questions,
                          @AppOption(description = "The characters used to separate multiple questions") @Nullable String delimiter) {
        if (!cobalt.config.checkIsQotdManager(event)) return;
        final JDA jda = event.getJDA();
        final User user = event.getUser();

        // Get questions
        List<String> questionList = List.of(questions);
        if (delimiter != null) questionList = List.of(questions.split(Pattern.quote(delimiter)));

        // Defer reply
        event.deferReply(true).queue();

        // Check question similarity
        final Map<String, Map.Entry<CoQuestion, Double>> similarQuestions = new HashMap<>();
        final List<String> questionListLower = questionList.stream()
                .map(question -> question.toLowerCase().trim())
                .toList();
        final JaroWinkler jaroWinkler = new JaroWinkler();
        for (final CoQuestion qotd : cobalt.data.global.qotds) {
            final String qotdLower = qotd.question.toLowerCase().trim();
            for (final String question : questionListLower) {
                final double similarity = jaroWinkler.similarity(question, qotdLower);
                if (similarity > 0.75) similarQuestions.put(question, Map.entry(qotd, similarity));
            }
        }

        // Add to database
        final List<CoQuestion> coQuestions = new ArrayList<>();
        for (final String question : questionList) {
            final int id = cobalt.data.global.getNextQotdId();
            final CoQuestion coQuestion = new CoQuestion(jda, id, question, user.getIdLong(), 0);
            cobalt.data.global.qotds.add(coQuestion);
            coQuestions.add(coQuestion);
        }

        // Reply
        final WebhookMessageEditAction<Message> action = event.getHook().editOriginalEmbeds(cobalt.messages.getEmbed("command", "qotd", "add", "success")
                        .replace("%questions%", coQuestions.stream()
                                .map(question -> "**" + question.id + ":** " + question.question)
                                .collect(Collectors.joining("\n"))).build());
        // Button
        if (!similarQuestions.isEmpty()) {
            action.setActionRow(Components.dangerButton(buttonEvent -> {
                // Build reply
                final StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Map.Entry<CoQuestion, Double>> entry : similarQuestions.entrySet()) {
                    final Map.Entry<CoQuestion, Double> value = entry.getValue();
                    final CoQuestion match = value.getKey();
                    stringBuilder.append("**Input:** \"").append(entry.getKey()).append("\"\n**Match:** ").append(match.id).append(" \"").append(match.question).append("\"\n**Percent:** ").append(Math.round(value.getValue() * 100)).append("%\n\n");
                }
                // Send reply
                final String text = stringBuilder.toString();
                if (text.length() > 2000) {
                    event.reply("Too many similar questions to display!").setEphemeral(true).queue();
                    return;
                }
                event.reply(text).setEphemeral(true).queue();
            }).build("Similar Questions"));
        }
        // Send
        action.queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotd",
            subcommand = "remove",
            description = "Remove a QOTD from the database")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID(s) of the question(s) to remove (use ',' to separate for multiple)", autocomplete = AC_REMOVE_IDS) @NotNull String ids) {
        if (!cobalt.config.checkIsQotdManager(event)) return;

        // Defer reply
        event.deferReply(true).queue();

        // Remove questions
        final List<CoQuestion> removedQuestions = new ArrayList<>();
        for (final String id : ids.split(",")) {
            final Integer idInt = CoMapper.toInt(id);
            if (idInt == null) continue;
            final CoQuestion question = cobalt.data.global.getQuestion(idInt);
            if (question == null) continue;
            cobalt.data.global.qotds.remove(question);
            removedQuestions.add(question);
        }

        // Reply
        event.getHook().editOriginal("Removed " + removedQuestions.size() + " question(s)\n" + removedQuestions.stream()
                        .map(this::formatQuestion)
                        .collect(Collectors.joining("\n")))
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "qotd",
            subcommand = "channel",
            description = "Sets the channel for the QOTD to be sent in")
    @UserPermissions(Permission.MANAGE_CHANNEL)
    public void channelCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The channel to send the QOTD in. Leave empty to remove QOTD") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);

        // Remove the QOTD channel
        if (channel == null) {
            coGuild.qotdChannel = null;
            event.reply("The QOTD channel for `" + guild.getName() + "` has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTD channel
        coGuild.qotdChannel = channel.getIdLong();
        event.reply("The QOTD channel for `" + guild.getName() + "` has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "qotd",
            subcommand = "role",
            description = "Sets the role that will be pinged for QOTD")
    @UserPermissions(Permission.MANAGE_ROLES)
    public void roleCommand(@NotNull GuildSlashEvent event,
                            @AppOption(description = "The role to ping for QOTD. Leave empty to remove QOTD") @Nullable Role role) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);

        // Remove the QOTD role
        if (role == null) {
            coGuild.qotdRole = null;
            event.reply("The QOTD role for **" + guild.getName() + "** has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTD role
        coGuild.qotdRole = role.getIdLong();
        event.reply("The QOTD role for **" + guild.getName() + "** has been set to " + role.getAsMention()).setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_LIST_USER) @NotNull
    public List<Command.Choice> acListUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isQotdManager(event.getUser())) return List.of();
        return cobalt.data.global.qotds.stream()
                .map(question -> {
                    final User user = question.getUser().complete();
                    if (user == null) return null;
                    return new Command.Choice(user.getAsTag(), user.getIdLong());
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @AutocompletionHandler(name = AC_LIST_USES) @NotNull
    public List<String> acListUses(@NotNull CommandAutoCompleteInteractionEvent event,
                                   @AppOption @Nullable String user) {
        final Stream<CoQuestion> questions = getQuestionStream(event, user);
        return questions == null ? List.of() : questions
                .map(question -> String.valueOf(question.used))
                .toList();
    }

    @AutocompletionHandler(name = AC_LIST_ID) @NotNull
    public List<String> acListId(@NotNull CommandAutoCompleteInteractionEvent event,
                                 @AppOption @Nullable String user,
                                 @AppOption @Nullable Integer uses) {
        Stream<CoQuestion> questions = getQuestionStream(event, user);
        if (questions == null) return List.of();
        if (uses != null) questions = questions.filter(question -> question.used <= uses);
        return questions
                .map(question -> String.valueOf(question.id))
                .toList();
    }

    @AutocompletionHandler(name = AC_REMOVE_IDS) @NotNull
    public List<String> acRemoveIds(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isQotdManager(event.getUser())) return List.of();
        return cobalt.data.global.qotds.stream()
                .map(question -> String.valueOf(question.id))
                .toList();
    }

    @NotNull
    private String formatQuestion(@NotNull CoQuestion question) {
        return "**" + question.id + "(<@" + question.user + ">):** " + question.question + " *(" + question.used + ")*";
    }

    @Nullable
    private Stream<CoQuestion> getQuestionStream(@NotNull CommandAutoCompleteInteractionEvent event, @Nullable String user) {
        if (!cobalt.config.isQotdManager(event.getUser())) return null;
        final Long userId = CoMapper.toLong(user);
        final Stream<CoQuestion> questions = cobalt.data.global.qotds.stream();
        return userId == null ? questions : questions.filter(question -> question.user == userId);
    }
}
