package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.api.components.event.StringSelectEvent;
import io.github.freya022.botcommands.api.modals.Modal;
import io.github.freya022.botcommands.api.modals.ModalEvent;
import io.github.freya022.botcommands.api.modals.Modals;
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler;
import io.github.freya022.botcommands.api.modals.annotations.ModalInput;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.data.DataObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.LazyLibrary;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Command
public class Embed extends ApplicationCommand {
    @NotNull private static final String BUTTON_EXPORT = "EmbedCmd.button.export";
    @NotNull private static final String BUTTON_SEND = "EmbedCmd.button.send";
    @NotNull private static final String MENU_FIELD = "EmbedCmd.menu.field";
    @NotNull private static final String MODAL_COLOR = "EmbedCmd.modal.color";
    @NotNull private static final String MODAL_AUTHOR = "EmbedCmd.modal.author";
    @NotNull private static final String MODAL_TITLE = "EmbedCmd.modal.title";
    @NotNull private static final String MODAL_DESCRIPTION = "EmbedCmd.modal.description";
    @NotNull private static final String MODAL_FIELD = "EmbedCmd.modal.field";
    @NotNull private static final String MODAL_MEDIA = "EmbedCmd.modal.media";
    @NotNull private static final String MODAL_FOOTER = "EmbedCmd.modal.footer";

    @NotNull private final LazyLibrary library;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus selectMenus;
    @NotNull private final Modals modals;

    public Embed(@NotNull LazyLibrary library, @NotNull Buttons buttons, @NotNull SelectMenus selectMenus, @NotNull Modals modals) {
        this.library = library;
        this.buttons = buttons;
        this.selectMenus = selectMenus;
        this.modals = modals;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "embed",
            description = "Create an embed")
    public void embedCommand(@NotNull GlobalSlashEvent event,
                             @SlashOption(description = "The JSON to use for the embed") @Nullable String json) {
        // Get Buttons
        final List<Button> actionRow = new ArrayList<>();
        actionRow.add(buttons.primary("Export JSON", LazyEmoji.UP_CLEAR_DARK.emoji).persistent()
                .bindTo(BUTTON_EXPORT)
                .build());
        if (!event.getChannel().getType().equals(ChannelType.PRIVATE) && library.isOwner(event.getUser().getIdLong())) {
            actionRow.add(buttons.success("Send", LazyEmoji.CHAT_CLEAR.emoji).persistent()
                    .bindTo(BUTTON_SEND)
                    .build());
        }

        // Reply
        event.replyEmbeds((json == null ? new LazyEmbed().setTitle("N/A") : new LazyEmbed(json)).build())
                .addComponents(
                        ActionRow.of(selectMenus.stringSelectMenu().persistent()
                                .bindTo(MENU_FIELD)
                                .addOption("Color", "color", Emoji.fromUnicode("\uD83C\uDFA8"))
                                .addOption("Author", "author", "Author name / Author URL / Author icon URL", Emoji.fromUnicode("✍"))
                                .addOption("Title", "title", "Title text / Title URL")
                                .addOption("Description", "description", Emoji.fromUnicode("\uD83D\uDCC4"))
                                .addOption("Field", "field", "Add a new field (Name / Value / Inline)", Emoji.fromUnicode("\uD83D\uDCCB"))
                                .addOption("Media", "media", "Thumbnail / Image", Emoji.fromUnicode("\uD83D\uDDBC"))
                                .addOption("Footer", "footer", "Footer text / Footer icon URL / Timestamp", Emoji.fromUnicode("\uD83E\uDDB6")).build()),
                        ActionRow.of(actionRow))
                .setEphemeral(true)
                .queue();
    }

    @JDAButtonListener(BUTTON_EXPORT)
    public void buttonExport(@NotNull ButtonEvent event) {
        final MessageEmbed embed = event.getMessage().getEmbeds().getFirst();
        final DataObject data = embed.toData();

        // Convert timestamp to milliseconds
        final OffsetDateTime timestamp = embed.getTimestamp();
        if (timestamp != null) data.put("timestamp", timestamp.getLong(ChronoField.INSTANT_SECONDS) * 1000 + timestamp.getLong(ChronoField.MILLI_OF_SECOND));

        // Reply
        event.reply("```json\n" + data + "\n```").setEphemeral(true).queue();
    }

    @JDAButtonListener(BUTTON_SEND)
    public void buttonSend(@NotNull ButtonEvent event) {
        if (!library.isOwner(event.getUser().getIdLong())) {
            event.deferEdit().queue();
            return;
        }
        event.getChannel().sendMessageEmbeds(event.getMessage().getEmbeds().getFirst()).queue();
    }

    @JDASelectMenuListener(MENU_FIELD)
    public void menuField(@NotNull StringSelectEvent event) {
        final MessageEmbed embed = event.getMessage().getEmbeds().getFirst();

        final Modal modal = switch (event.getValues().getFirst()) {
            case "color" -> {
                final Color color = embed.getColor();
                yield modals.create("Color")
                        .bindTo(MODAL_COLOR)
                        .timeout(10, TimeUnit.MINUTES, () -> {})
                        .addComponents(createTextInput("color", "Color", TextInputStyle.SHORT, false, 7, "Hexadecimal color code",  color == null ? null : String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())))
                        .build();
            }
            case "author" -> {
                final MessageEmbed.AuthorInfo author = embed.getAuthor();
                yield modals.create("Author")
                        .bindTo(MODAL_AUTHOR)
                        .timeout(10, TimeUnit.MINUTES, () -> {})
                        .addComponents(
                                createTextInput("authorName", "Author name", TextInputStyle.SHORT, false, MessageEmbed.AUTHOR_MAX_LENGTH, null, author == null ? null : author.getName()),
                                createTextInput("authorUrl", "Author URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", author == null ? null : author.getUrl()),
                                createTextInput("authorIconUrl", "Author icon URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", author == null ? null : author.getIconUrl()))
                        .build();
            }
            case "title" -> modals.create("Title")
                    .bindTo(MODAL_TITLE)
                    .timeout(10, TimeUnit.MINUTES, () -> {})
                    .addComponents(
                            createTextInput("titleText", "Title text", TextInputStyle.SHORT, false, MessageEmbed.TITLE_MAX_LENGTH, null, embed.getTitle()),
                            createTextInput("titleUrl", "Title URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", embed.getUrl()))
                    .build();
            case "description" -> modals.create("Description")
                    .bindTo(MODAL_DESCRIPTION)
                    .timeout(10, TimeUnit.MINUTES, () -> {})
                    .addComponents(createTextInput("description", "Description", TextInputStyle.PARAGRAPH, false, 4000, null, embed.getDescription()))
                    .build();
            case "field" -> modals.create("Field")
                        .bindTo(MODAL_FIELD)
                    .timeout(10, TimeUnit.MINUTES, () -> {})
                    .addComponents(
                            createTextInput("fieldName", "Name", TextInputStyle.SHORT, true, MessageEmbed.TITLE_MAX_LENGTH, null, null),
                            createTextInput("fieldValue", "Value", TextInputStyle.PARAGRAPH, true, MessageEmbed.VALUE_MAX_LENGTH, null, null),
                            createTextInput("fieldInline", "Inline", TextInputStyle.SHORT, false, 5, "true/false", "false"))
                    .build();
            case "media" -> {
                final MessageEmbed.Thumbnail thumbnail = embed.getThumbnail();
                final MessageEmbed.ImageInfo image = embed.getImage();
                yield modals.create("Media")
                        .bindTo(MODAL_MEDIA)
                        .timeout(10, TimeUnit.MINUTES, () -> {})
                        .addComponents(
                                createTextInput("thumbnail", "Thumbnail", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", thumbnail == null ? null : thumbnail.getUrl()),
                                createTextInput("image", "Image", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", image == null ? null : image.getUrl()))
                        .build();
            }
            case "footer" -> {
                final MessageEmbed.Footer footer = embed.getFooter();
                final TemporalAccessor timestamp = embed.getTimestamp();
                yield modals.create("Footer")
                        .bindTo(MODAL_FOOTER)
                        .timeout(10, TimeUnit.MINUTES, () -> {})
                        .addComponents(
                                createTextInput("footerText", "Footer text", TextInputStyle.SHORT, false, MessageEmbed.TEXT_MAX_LENGTH, null, footer == null ? null : footer.getText()),
                                createTextInput("footerIconUrl", "Footer icon", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", footer == null ? null : footer.getIconUrl()),
                                createTextInput("timestamp", "Timestamp", TextInputStyle.SHORT, false, 10, "Epoch time or 'now'", timestamp == null ? null : String.valueOf(Instant.from(timestamp).toEpochMilli())))
                        .build();
            }
            default -> null;
        };

        if (modal != null) event.replyModal(modal).queue();
    }

    @ModalHandler(MODAL_COLOR)
    public void modalColor(@NotNull ModalEvent event,
                           @ModalInput("color") @NotNull String color) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message == null) return;
        final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().getFirst());

        // Get color
        color = processInput(color);
        if (color != null) {
            if (!color.startsWith("#")) color = "#" + color;
            try {
                builder.setColor(Color.decode(color));
            } catch (final NumberFormatException e) {
                error(event, "color", color);
                return;
            }
        }

        // Edit embed
        editEmbed(event, builder);
    }

    @ModalHandler(MODAL_AUTHOR)
    public void modalAuthor(@NotNull ModalEvent event,
                            @ModalInput("authorName") @NotNull String authorName,
                            @ModalInput("authorUrl") @NotNull String authorUrl,
                            @ModalInput("authorIconUrl") @NotNull String authorIconUrl) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message == null) return;
        final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().getFirst());

        // Set author
        try {
            builder.setAuthor(processInput(authorName), processInput(authorUrl), processInput(authorIconUrl));
        } catch (final IllegalArgumentException e) {
            error(event, "author URL or author icon URL", e.getMessage());
            return;
        }

        // Edit embed
        editEmbed(event, builder);
    }

    @ModalHandler(MODAL_TITLE)
    public void modalTitle(@NotNull ModalEvent event,
                           @ModalInput("titleText") @NotNull String titleText,
                           @ModalInput("titleUrl") @NotNull String titleUrl) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message == null) return;
        final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().getFirst());

        // Set title
        try {
            builder.setTitle(processInput(titleText), processInput(titleUrl));
        } catch (final IllegalArgumentException e) {
            error(event, "title URL", titleUrl);
            return;
        }

        // Edit embed
        editEmbed(event, builder);
    }

    @ModalHandler(MODAL_DESCRIPTION)
    public void modalDescription(@NotNull ModalEvent event,
                                 @ModalInput("description") @NotNull String description) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message != null) editEmbed(event, new EmbedBuilder(message.getEmbeds().getFirst()).setDescription(description));
    }

    @ModalHandler(MODAL_FIELD)
    public void modalField(@NotNull ModalEvent event,
                           @ModalInput("fieldName") @NotNull String fieldName,
                           @ModalInput("fieldValue") @NotNull String fieldValue,
                           @ModalInput("fieldInline") @NotNull String fieldInline) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message != null) editEmbed(event, new EmbedBuilder(message.getEmbeds().getFirst()).addField(fieldName, fieldValue, Boolean.parseBoolean(fieldInline)));
    }

    @ModalHandler(MODAL_MEDIA)
    public void modalMedia(@NotNull ModalEvent event,
                           @ModalInput("thumbnail") @NotNull String thumbnail,
                           @ModalInput("image") @NotNull String image) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message == null) return;
        final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().getFirst());

        // Set thumbnail
        try {
            builder.setThumbnail(processInput(thumbnail));
        } catch (final IllegalArgumentException e) {
            error(event, "thumbnail", thumbnail);
            return;
        }

        // Set image
        try {
            builder.setImage(processInput(image));
        } catch (final IllegalArgumentException e) {
            error(event, "image", image);
            return;
        }

        // Edit embed
        editEmbed(event, builder);
    }

    @ModalHandler(MODAL_FOOTER)
    public void modalFooter(@NotNull ModalEvent event,
                            @ModalInput("footerText") @NotNull String footerText,
                            @ModalInput("footerIconUrl") @NotNull String footerIconUrl,
                            @ModalInput("timestamp") @NotNull String timestamp) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message == null) return;
        final EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().getFirst());

        // Set footer
        try {
            builder.setFooter(processInput(footerText), processInput(footerIconUrl));
        } catch (final IllegalArgumentException e) {
            error(event, "footer icon URL", footerIconUrl);
            return;
        }

        // Get timestamp
        timestamp = processInput(timestamp);
        TemporalAccessor timestampValue = null;
        if (timestamp != null) {
            if (timestamp.equalsIgnoreCase("now")) {
                timestampValue = OffsetDateTime.now();
            } else {
                final Optional<Long> timestampLong = Mapper.toLong(timestamp);
                if (timestampLong.isEmpty()) {
                    error(event, "timestamp", timestamp);
                    return;
                } else try {
                    timestampValue = Instant.ofEpochMilli(timestampLong.get());
                } catch (final NumberFormatException e) {
                    error(event, "timestamp", timestamp);
                    return;
                }
            }
        }

        // Edit embed
        editEmbed(event, builder.setTimestamp(timestampValue));
    }

    @NotNull
    private Label createTextInput(@NotNull String inputName, @NotNull String label, @NotNull TextInputStyle style, boolean required, int maxLength, @Nullable String placeholder, @Nullable String value) {
        return Label.of(label, TextInput.create(inputName, style)
                .setRequired(required)
                .setMaxLength(maxLength)
                .setPlaceholder(placeholder)
                .setValue(value)
                .build());
    }

    @Nullable
    private String processInput(@NotNull String input) {
        return input.isEmpty() || input.equalsIgnoreCase("null") ? null : input;
    }

    private void error(@NotNull ModalEvent event, @NotNull String parameter, @Nullable String value) {
        event.getHook().editOriginal(LazyEmoji.NO + " **Invalid " + parameter + "**: `" + value + "`").queue();
    }

    private void editEmbed(@NotNull ModalEvent event, @NotNull EmbedBuilder builder) {
        try {
            event.getHook().editOriginal("").setEmbeds(builder.build()).queue();
        } catch (final IllegalStateException e) {
            error(event, "embed", e.getMessage());
        }
    }
}
