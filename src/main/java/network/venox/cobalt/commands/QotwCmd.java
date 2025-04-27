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
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.modals.Modals;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import com.freya02.botcommands.api.utils.ButtonContent;

import info.debatty.java.stringsimilarity.JaroWinkler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoQuestion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CommandMarker
public class QotwCmd extends ApplicationCommand {
    @NotNull private static final String AC_LIST_USER = "QotwCmd.ac.list.user";
    @NotNull private static final String AC_LIST_ID = "QotwCmd.ac.list.id";
    @NotNull private static final String AC_LIST_USES = "QotwCmd.ac.list.uses";
    @NotNull private static final String AC_REMOVE_IDS = "QotwCmd.ac.remove.ids";
    @NotNull private static final String BUTTON_SUGGESTION_APPROVE = "QotwCmd.button.suggestion.approve";
    @NotNull private static final String BUTTON_SUGGESTION_DENY = "QotwCmd.button.suggestion.deny";
    @NotNull private static final String BUTTON_SUGGESTION_EDIT = "QotwCmd.button.suggestion.edit";
    @NotNull private static final String MODAL_SUGGESTION_EDIT = "QotwCmd.modal.suggestion.edit";

    @NotNull private static final JaroWinkler JARO_WINKLER = new JaroWinkler();

    @Dependency private Cobalt cobalt;

    // GLOBAL

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "list",
            description = "List all (or a specific) QOTW questions in the database")
    public void listCommand(@NotNull GlobalSlashEvent event,
                            @AppOption(description = "The user ID of who created the question", autocomplete = AC_LIST_USER) @Nullable String user,
                            @AppOption(description = "The number of times the question has been used", autocomplete = AC_LIST_USES) @Nullable Integer uses,
                            @AppOption(description = "The ID of the question to get", autocomplete = AC_LIST_ID) @Nullable Integer id) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        final InteractionHook hook = event.getHook();
        event.deferReply(true).queue();

        // ID-specific
        if (id != null) {
            final CoQuestion question = cobalt.oldData.global.getQuestion(id);
            if (question == null) {
                hook.editOriginal(LazyEmoji.NO + " Question with ID `" + id + "` does not exist!").queue();
                return;
            }
            hook.editOriginal(question.getFormatted()).queue();
            return;
        }

        // Build embeds
        final List<MessageEmbed> embeds = new ArrayList<>();
        final StringBuilder stringBuilder = new StringBuilder();
        final Long userId = Mapper.toLong(user);
        final boolean isUserSpecific = userId != null;
        final boolean isUsesSpecific = uses != null;
        for (final CoQuestion question : cobalt.oldData.global.qotws) {
            if ((isUserSpecific && question.user != userId) || (isUsesSpecific && question.used > uses)) continue;
            final String line = question.getFormatted();
            if (stringBuilder.length() + line.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
                embeds.add(new LazyEmbed()
                        .setTitle("Questions " + (embeds.size() + 1))
                        .setDescription(stringBuilder.toString())
                        .build(cobalt));
                stringBuilder.setLength(0);
            }
            stringBuilder.append(line).append("\n");
        }
        if (!stringBuilder.isEmpty()) embeds.add(new LazyEmbed()
                .setTitle("Questions " + (embeds.size() + 1))
                .setDescription(stringBuilder.toString())
                .build(cobalt));

        // No questions
        if (embeds.isEmpty()) {
            hook.editOriginal(LazyEmoji.NO + " No questions found!").queue();
            return;
        }

        // Send single message
        if (embeds.size() == 1) {
            hook.editOriginalEmbeds(embeds.get(0)).queue();
            return;
        }

        // Send paginator message
        hook.editOriginal(LazyUtilities.getDefaultPaginator()
                        .useDeleteButton(false)
                        .setMaxPages(embeds.size())
                        .setPaginatorSupplier((paginator, editBuilder, components, page) -> embeds.get(page))
                        .build().get())
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "similar",
            description = "List all similar QOTW questions in the database")
    public void similarCommand(@NotNull GlobalSlashEvent event,
                               @AppOption(description = "The question to find similar questions to") @Nullable String question) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        final InteractionHook hook = event.getHook();
        event.deferReply(true).queue();

        // Get similar question groups
        final List<List<CoQuestion>> groups;
        if (question != null) {
            final List<CoQuestion> similarQuestions = getSimilarQuestions(null, List.of(question)).values().stream()
                    .flatMap(List::stream)
                    .map(Map.Entry::getKey)
                    .toList();
            groups = similarQuestions.isEmpty() ? List.of() : List.of(similarQuestions);
        } else {
            groups = new ArrayList<>();
            final List<CoQuestion> questions = new ArrayList<>(cobalt.oldData.global.qotws);
            for (final CoQuestion coQuestion : cobalt.oldData.global.qotws) {
                if (!questions.contains(coQuestion)) continue;
                final List<CoQuestion> input = new ArrayList<>(questions);
                input.remove(coQuestion);
                if (input.isEmpty()) break;
                final List<CoQuestion> similarQuestions = new ArrayList<>(getSimilarQuestions(input, List.of(coQuestion.question)).values().stream()
                        .flatMap(List::stream)
                        .map(Map.Entry::getKey)
                        .toList());
                if (similarQuestions.isEmpty()) continue;
                similarQuestions.add(0, coQuestion);
                groups.add(similarQuestions);
                questions.removeAll(similarQuestions);
                if (questions.isEmpty()) break;
            }
        }

        // Check if empty
        if (groups.isEmpty()) {
            hook.editOriginal(LazyEmoji.NO + " No similar questions found!").queue();
            return;
        }

        // Build embeds
        final List<MessageEmbed> embeds = new ArrayList<>();
        for (var i = 0; i < groups.size(); i++) embeds.add(new LazyEmbed()
                .setTitle("Similar Questions: Group " + (i + 1) + "/" + groups.size())
                .setDescription(groups.get(i).stream()
                        .map(CoQuestion::getFormatted)
                        .collect(Collectors.joining("\n")))
                .build(cobalt));

        // Send single message
        if (embeds.size() == 1) {
            hook.editOriginalEmbeds(embeds.get(0)).queue();
            return;
        }

        // Send paginator message
        hook.editOriginal(LazyUtilities.getDefaultPaginator()
                        .useDeleteButton(false)
                        .setMaxPages(embeds.size())
                        .setPaginatorSupplier((paginator, editBuilder, components, page) -> embeds.get(page))
                        .build().get())
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "duplicates",
            description = "List and remove all exact duplicate questions")
    public void duplicatesCommand(@NotNull GlobalSlashEvent event) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        final InteractionHook hook = event.getHook();
        event.deferReply(true).queue();

        // Get questions
        final List<List<CoQuestion>> groups = new ArrayList<>();
        final List<CoQuestion> questions = new ArrayList<>(cobalt.oldData.global.qotws);
        for (final CoQuestion coQuestion : cobalt.oldData.global.qotws) {
            if (!questions.contains(coQuestion)) continue;
            final List<CoQuestion> duplicates = new ArrayList<>(questions.stream()
                    .filter(qotw -> qotw.question.toLowerCase().trim().equals(coQuestion.question.toLowerCase().trim()))
                    .toList());
            if (duplicates.size() <= 1) continue;
            groups.add(duplicates);
            questions.removeAll(duplicates);
            if (questions.size() <= 1) break;
        }

        // Check if empty
        if (groups.isEmpty()) {
            hook.editOriginal(LazyEmoji.NO + " No duplicate questions were found!").queue();
            return;
        }

        // Build embeds
        final List<MessageEmbed> embeds = new ArrayList<>();
        for (var i = 0; i < groups.size(); i++) embeds.add(new LazyEmbed()
                .setTitle("Duplicates: Group " + (i + 1) + "/" + groups.size())
                .setDescription(groups.get(i).stream()
                        .map(CoQuestion::getFormatted)
                        .collect(Collectors.joining("\n")))
                .build(cobalt));

        // Send single message
        if (embeds.size() == 1) {
            hook.editOriginalEmbeds(embeds.get(0)).queue();
            return;
        }

        // Send paginator message
        final List<Integer> cleaned = new ArrayList<>();
        final AtomicBoolean cleanedAll = new AtomicBoolean(false);
        hook.editOriginal(LazyUtilities.getDefaultPaginator()
                        .useDeleteButton(false)
                        .setMaxPages(embeds.size())
                        .setPaginatorSupplier((paginator, editBuilder, components, page) -> {
                            // Get "this group" button
                            Button thisGroup = Components.dangerButton(buttonEvent -> {
                                // Remove questions
                                final List<CoQuestion> duplicates = groups.get(page);
                                duplicates.remove(0);
                                cobalt.oldData.global.qotws.removeAll(duplicates);
                                cleaned.add(page);
                                // Edit embed
                                final List<LayoutComponent> messageComponents = buttonEvent.getMessage().getComponents();
                                final List<Button> rowTwo = messageComponents.get(1).getButtons();
                                buttonEvent.editMessage(buttonEvent.getMessage().getContentRaw() + "\n" + LazyEmoji.YES + " Cleaned **Group " + (page + 1) + "** *(" + duplicates.size() + " questions)*")
                                        .setComponents(messageComponents.get(0), ActionRow.of(rowTwo.get(0).asDisabled(), rowTwo.get(1)))
                                        .queue();
                            }).build(LazyEmoji.TRASH_CLEAR_DARK.getButtonContent("Clean this group"));
                            // Get "all groups" button
                            Button allGroups = Components.dangerButton(buttonEvent -> {
                                // Remove questions
                                final List<CoQuestion> duplicates = groups.stream()
                                        .flatMap(list -> list.stream().skip(1))
                                        .toList();
                                cobalt.oldData.global.qotws.removeAll(duplicates);
                                cleanedAll.set(true);
                                // Edit embed
                                final List<LayoutComponent> messageComponents = buttonEvent.getMessage().getComponents();
                                buttonEvent.editMessage(LazyEmoji.YES + " Cleaned **ALL** groups *(" + duplicates.size() + " questions)*")
                                        .setComponents(messageComponents.get(0), messageComponents.get(1).asDisabled())
                                        .queue();
                            }).build(LazyEmoji.TRASH_CLEAR_DARK.getButtonContent("Clean ALL groups"));

                            // Add buttons
                            final boolean cleanedAllValue = cleanedAll.get();
                            if (cleanedAllValue || cleaned.contains(page)) thisGroup = thisGroup.asDisabled();
                            if (cleanedAllValue) allGroups = allGroups.asDisabled();
                            components.addComponents(thisGroup, allGroups);

                            // Return embed
                            return embeds.get(page);
                        })
                        .build().get())
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "add",
            description = "Add a QOTW to the database")
    public void addCommand(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The questions to add") @NotNull String questions,
                           @AppOption(description = "The characters used to separate multiple questions") @Nullable String delimiter) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        final long userId = event.getUser().getIdLong();
        final List<String> questionList = delimiter == null ? List.of(questions) : List.of(questions.split(Pattern.quote(delimiter)));

        // Get buttons
        final List<Button> buttons = new ArrayList<>();
        buttons.add(Components.successButton(buttonEvent -> {
            questionList.forEach(question -> cobalt.oldData.global.qotws.add(new CoQuestion(cobalt, cobalt.oldData.global.getNextQotwId(), question, userId, 0)));
            buttonEvent.reply(LazyEmoji.YES + " Added **" + questionList.size() + "** question(s)!").setEphemeral(true).queue();
        }).build(LazyEmoji.YES_CLEAR.getButtonContent("Continue")));
        final Map<String, List<Map.Entry<CoQuestion, Double>>> similarQuestions = getSimilarQuestions(null, questionList);
        if (!similarQuestions.isEmpty()) buttons.add(Components.dangerButton(buttonEvent -> {
            // Build reply
            final StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<Map.Entry<CoQuestion, Double>>> entry : similarQuestions.entrySet()) {
                final Map.Entry<CoQuestion, Double> value = entry.getValue().get(0);
                final CoQuestion match = value.getKey();
                builder.append("**Input:** \"").append(entry.getKey()).append("\"\n**Match:** ").append(match.id).append(" \"").append(match.question).append("\"\n**Percent:** ").append(Math.round(value.getValue() * 100)).append("%\n\n");
            }
            // Send reply
            final String text = builder.toString();
            if (text.length() > 2000) {
                buttonEvent.reply(LazyEmoji.NO + " Too many similar questions to display!").setEphemeral(true).queue();
                return;
            }
            buttonEvent.reply(text).setEphemeral(true).queue();
        }).build(new ButtonContent("Similar questions", Emoji.fromUnicode("❓"))));

        // Send reply
        event.replyEmbeds(new LazyEmbed()
                        .setTitle("Do you want to continue?")
                        .setDescription(questionList.stream()
                                .map(question -> "- " + question)
                                .collect(Collectors.joining("\n")))
                        .build(cobalt))
                .setActionRow(buttons)
                .setEphemeral(true)
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "remove",
            description = "Remove a QOTW from the database")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                              @AppOption(description = "The ID(s) of the question(s) to remove (use ',' to separate for multiple)", autocomplete = AC_REMOVE_IDS) @NotNull String ids) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        event.deferReply(true).queue();

        // Remove questions
        final List<CoQuestion> removedQuestions = new ArrayList<>();
        for (final String id : ids.split(",")) {
            final Integer idInt = Mapper.toInt(id);
            if (idInt == null) continue;
            final CoQuestion question = cobalt.oldData.global.getQuestion(idInt);
            if (question == null) continue;
            cobalt.oldData.global.qotws.remove(question);
            removedQuestions.add(question);
        }

        // Reply
        event.getHook().editOriginal(LazyEmoji.YES + " Removed " + removedQuestions.size() + " question(s)\n" + removedQuestions.stream()
                        .map(CoQuestion::getFormatted)
                        .collect(Collectors.joining("\n")))
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "qotw",
            subcommand = "suggest",
            description = "Suggest a question to be used in a future QOTW")
    public void suggestCommand(@NotNull GlobalSlashEvent event,
                               @AppOption(description = "The question to suggest") @NotNull String question) {
        final TextChannel botManagerChat = cobalt.config.getGuildBotManagerChat();
        if (botManagerChat == null) {
            event.reply(LazyEmoji.NO + " Bot Manager channel not set!").setEphemeral(true).queue();
            return;
        }
        event.deferReply(true).queue();
        final User user = event.getUser();
        final long userId = user.getIdLong();
        final LazyEmbed embed = new LazyEmbed()
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setTitle(LazyEmoji.MAYBE + " QOTW Suggestion")
                .setDescription(question);
        final Map<String, List<Map.Entry<CoQuestion, Double>>> similarQuestions = getSimilarQuestions(null, List.of(question));
        if (!similarQuestions.isEmpty()) {
            final Map.Entry<CoQuestion, Double> similarQuestion = similarQuestions.get(question).get(0);
            final CoQuestion match = similarQuestion.getKey();
            embed.addField(LazyEmoji.WARNING + " Similar question found!", "**ID:** " + match.id + "\n**Question:** " + match.question + "\n**Similarity:** " + Math.round(similarQuestion.getValue() * 100) + "%", false);
        }
        botManagerChat.sendMessage("<@&" + cobalt.config.guildBotManager + ">").setEmbeds(embed.build(cobalt))
                .addActionRow(
                        Components.successButton(BUTTON_SUGGESTION_APPROVE, userId).build(new ButtonContent("Approve", LazyEmoji.YES_CLEAR.getEmoji())),
                        Components.dangerButton(BUTTON_SUGGESTION_DENY, userId).build(new ButtonContent("Deny", LazyEmoji.NO_CLEAR_DARK.getEmoji())),
                        Components.primaryButton(BUTTON_SUGGESTION_EDIT).build(new ButtonContent("Edit", Emoji.fromUnicode("✏"))))
                .flatMap(msg -> event.getHook().editOriginal(LazyEmoji.YES + " Suggestion sent!"))
                .queue();
    }

    // GUILD

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "qotw",
            subcommand = "channel",
            description = "Sets the channel for the QOTW to be sent in")
    @UserPermissions(Permission.MANAGE_CHANNEL)
    public void channelCommand(@NotNull GuildSlashEvent event,
                               @AppOption(description = "The channel to send the QOTW in. Leave empty to remove QOTW") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.oldData.getGuild(guild);

        // Remove the QOTW channel
        if (channel == null) {
            coGuild.qotwChannel = null;
            event.reply(LazyEmoji.YES + " The QOTW channel for `" + guild.getName() + "` has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTW channel
        coGuild.qotwChannel = channel.getIdLong();
        event.reply(LazyEmoji.YES + " The QOTW channel for `" + guild.getName() + "` has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "qotw",
            subcommand = "role",
            description = "Sets the role that will be pinged for QOTW")
    @UserPermissions(Permission.MANAGE_ROLES)
    public void roleCommand(@NotNull GuildSlashEvent event,
                            @AppOption(description = "The role to ping for QOTW. Leave empty to remove QOTW") @Nullable Role role) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.oldData.getGuild(guild);

        // Remove the QOTW role
        if (role == null) {
            coGuild.qotwRole = null;
            event.reply(LazyEmoji.YES + " The QOTW role for **" + guild.getName() + "** has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTW role
        coGuild.qotwRole = role.getIdLong();
        event.reply(LazyEmoji.YES + " The QOTW role for **" + guild.getName() + "** has been set to " + role.getAsMention()).setEphemeral(true).queue();
    }

    // AUTO-COMPLETION

    @AutocompletionHandler(name = AC_LIST_USER) @NotNull
    public List<Command.Choice> acListUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isBotManager(event.getUser())) return List.of();
        return cobalt.oldData.global.qotws.stream()
                .map(question -> new Command.Choice(question.getUser().complete().getName(), question.user))
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
        if (!cobalt.config.isBotManager(event.getUser())) return List.of();
        return cobalt.oldData.global.qotws.stream()
                .map(question -> String.valueOf(question.id))
                .toList();
    }

    // COMPONENTS

    @JDAButtonListener(name = BUTTON_SUGGESTION_APPROVE)
    public void buttonSuggestionApprove(@NotNull ButtonEvent event,
                                        @AppOption long userId) {
        if (!cobalt.config.checkIsBotManager(event)) return;

        // Get question
        final Message message = event.getMessage();
        final MessageEmbed embed = message.getEmbeds().get(0);
        final String question = embed.getDescription();
        if (question == null) return;

        cobalt.oldData.global.qotws.add(new CoQuestion(cobalt, cobalt.oldData.global.getNextQotwId(), question, userId, 0));
        final LazyEmbed newEmbed = new LazyEmbed(embed)
                        .setTitle(LazyEmoji.YES + " QOTW Suggestion")
                        .setColor(Color.GREEN);
        final Button button = Components.successButton(buttonEvent -> {}).build(new ButtonContent("Approved by " + event.getUser().getName(), LazyEmoji.YES_CLEAR.getEmoji())).asDisabled();
        event.deferEdit()
                .flatMap(hook -> message.editMessageEmbeds(newEmbed.build(cobalt)).setActionRow(button))
                .flatMap(msg -> msg.getJDA().retrieveUserById(userId))
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessageEmbeds(newEmbed.setAuthor(null).build(cobalt)).setActionRow(button))
                .queue();
    }

    @JDAButtonListener(name = BUTTON_SUGGESTION_DENY)
    public void buttonSuggestionDeny(@NotNull ButtonEvent event,
                                     @AppOption long userId) {
        if (!cobalt.config.checkIsBotManager(event)) return;
        final Message message = event.getMessage();
        final LazyEmbed newEmbed = new LazyEmbed(message.getEmbeds().get(0))
                .setTitle(LazyEmoji.NO + " QOTW Suggestion")
                .setColor(Color.RED);
        final Button button = Components.dangerButton(buttonEvent -> {}).build(new ButtonContent("Denied by " + event.getUser().getName(), LazyEmoji.NO_CLEAR_DARK.getEmoji())).asDisabled();
        event.deferEdit()
                .flatMap(hook -> message.editMessageEmbeds(newEmbed.build(cobalt)).setActionRow(button))
                .flatMap(msg -> event.getJDA().retrieveUserById(userId))
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessageEmbeds(newEmbed.setAuthor(null).build(cobalt)).setActionRow(button))
                .queue();
    }

    @JDAButtonListener(name = BUTTON_SUGGESTION_EDIT)
    public void buttonSuggestionEdit(@NotNull ButtonEvent event) {
        if (!cobalt.config.checkIsBotManager(event)) return;

        // Get question
        final Message message = event.getMessage();
        final MessageEmbed embed = message.getEmbeds().get(0);
        final String question = embed.getDescription();
        if (question == null) return;

        // Modal
        event.replyModal(Modals.create("Edit QOTW suggestion", MODAL_SUGGESTION_EDIT)
                .setTimeout(5, TimeUnit.MINUTES, () -> {})
                .addActionRow(Modals.createTextInput("question", "Question", TextInputStyle.PARAGRAPH)
                        .setValue(question)
                        .setPlaceholder("The edited question")
                        .build())
                .build()).queue();
    }

    @ModalHandler(name = MODAL_SUGGESTION_EDIT)
    public void modalSuggestionEdit(@NotNull ModalInteractionEvent event,
                                    @ModalInput(name = "question") @NotNull String question) {
        final Message message = event.getMessage();
        if (message != null) event.editMessageEmbeds(new LazyEmbed(message.getEmbeds().get(0)).setDescription(question).build(cobalt)).queue();
    }

    // HELPERS

    @Nullable
    private Stream<CoQuestion> getQuestionStream(@NotNull CommandAutoCompleteInteractionEvent event, @Nullable String user) {
        if (!cobalt.config.isBotManager(event.getUser())) return null;
        final Long userId = Mapper.toLong(user);
        final Stream<CoQuestion> questions = cobalt.oldData.global.qotws.stream();
        return userId == null ? questions : questions.filter(question -> question.user == userId);
    }

    @NotNull
    private Map<String, List<Map.Entry<CoQuestion, Double>>> getSimilarQuestions(@Nullable Collection<CoQuestion> questions, @NotNull Collection<String> questionsToCheck) {
        if (questions == null) questions = cobalt.oldData.global.qotws;
        final Map<String, List<Map.Entry<CoQuestion, Double>>> similarQuestions = new HashMap<>();
        for (final String question : questionsToCheck) {
            final List<Map.Entry<CoQuestion, Double>> similar = new ArrayList<>();
            final String questionClean = CoQuestion.clean(cobalt, question);
            for (final CoQuestion qotw : questions) {
                final double similarity = JARO_WINKLER.similarity(questionClean, qotw.getCleaned());
                if (similarity > 0.75) similar.add(Map.entry(qotw, similarity));
            }
            if (!similar.isEmpty()) similarQuestions.put(question, similar);
        }
        return similarQuestions;
    }
}
