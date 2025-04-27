package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;
import com.freya02.botcommands.api.modals.Modals;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import com.freya02.botcommands.api.utils.ButtonContent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.data.DataObject;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@CommandMarker
public class EmbedCmd extends ApplicationCommand {
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

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "embed",
            description = "Create an embed")
    public void embedCommand(@NotNull GlobalSlashEvent event,
                             @AppOption(description = "The JSON to use for the embed") @Nullable String json) {
        // Get Buttons
        final List<Button> buttons = new ArrayList<>();
        buttons.add(Components.primaryButton(BUTTON_EXPORT).build(new ButtonContent("Export JSON", LazyEmoji.UP_CLEAR_DARK.emoji)));
        if (!event.getChannel().getType().equals(ChannelType.PRIVATE) && cobalt.isOwner(event.getUser().getIdLong())) buttons.add(Components.successButton(BUTTON_SEND).build(new ButtonContent("Send", LazyEmoji.CHAT_CLEAR.emoji)));

        // Reply
        event.replyEmbeds((json == null ? new LazyEmbed().setTitle("N/A") : new LazyEmbed(json)).build(cobalt))
                .addActionRow(Components.stringSelectionMenu(MENU_FIELD)
                        .addOption("Color", "color", Emoji.fromUnicode("\uD83C\uDFA8"))
                        .addOption("Author", "author", "Author name / Author URL / Author icon URL", Emoji.fromUnicode("✍"))
                        .addOption("Title", "title", "Title text / Title URL")
                        .addOption("Description", "description", Emoji.fromUnicode("\uD83D\uDCC4"))
                        .addOption("Field", "field", "Add a new field (Name / Value / Inline)", Emoji.fromUnicode("\uD83D\uDCCB"))
                        .addOption("Media", "media", "Thumbnail / Image", Emoji.fromUnicode("\uD83D\uDDBC"))
                        .addOption("Footer", "footer", "Footer text / Footer icon URL / Timestamp", Emoji.fromUnicode("\uD83E\uDDB6")).build())
                .addActionRow(buttons)
                .setEphemeral(true)
                .queue();
    }

    @JDAButtonListener(name = BUTTON_EXPORT)
    public void buttonExport(@NotNull ButtonEvent event) {
        final MessageEmbed embed = event.getMessage().getEmbeds().getFirst();
        final DataObject data = embed.toData();

        // Convert timestamp to milliseconds
        final OffsetDateTime timestamp = embed.getTimestamp();
        if (timestamp != null) data.put("timestamp", timestamp.getLong(ChronoField.INSTANT_SECONDS) * 1000 + timestamp.getLong(ChronoField.MILLI_OF_SECOND));

        // Reply
        event.reply("```json\n" + data + "\n```").setEphemeral(true).queue();
    }

    @JDAButtonListener(name = BUTTON_SEND)
    public void buttonSend(@NotNull ButtonEvent event) {
        if (!cobalt.isOwner(event.getUser().getIdLong())) {
            event.deferEdit().queue();
            return;
        }
        event.getChannel().sendMessageEmbeds(event.getMessage().getEmbeds().getFirst()).queue();
    }

    @JDASelectionMenuListener(name = MENU_FIELD)
    public void menuField(@NotNull StringSelectionEvent event) {
        final MessageEmbed embed = event.getMessage().getEmbeds().getFirst();

        final Modal modal = switch (event.getValues().getFirst()) {
            case "color" -> {
                final Color color = embed.getColor();
                yield Modals.create("Color", MODAL_COLOR)
                        .setTimeout(10, TimeUnit.MINUTES, () -> {})
                        .addActionRow(createTextInput("color", "Color", TextInputStyle.SHORT, false, 7, "Hexadecimal color code",  color == null ? null : String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())).build())
                        .build();
            }
            case "author" -> {
                final MessageEmbed.AuthorInfo author = embed.getAuthor();
                yield Modals.create("Author", MODAL_AUTHOR)
                        .setTimeout(10, TimeUnit.MINUTES, () -> {})
                        .addActionRow(createTextInput("authorName", "Author name", TextInputStyle.SHORT, false, MessageEmbed.AUTHOR_MAX_LENGTH, null, author == null ? null : author.getName()).build())
                        .addActionRow(createTextInput("authorUrl", "Author URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", author == null ? null : author.getUrl()).build())
                        .addActionRow(createTextInput("authorIconUrl", "Author icon URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", author == null ? null : author.getIconUrl()).build())
                        .build();
            }
            case "title" -> Modals.create("Title", MODAL_TITLE)
                    .setTimeout(10, TimeUnit.MINUTES, () -> {})
                    .addActionRow(createTextInput("titleText", "Title text", TextInputStyle.SHORT, false, MessageEmbed.TITLE_MAX_LENGTH, null, embed.getTitle()).build())
                    .addActionRow(createTextInput("titleUrl", "Title URL", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", embed.getUrl()).build())
                    .build();
            case "description" -> Modals.create("Description", MODAL_DESCRIPTION)
                    .setTimeout(10, TimeUnit.MINUTES, () -> {})
                    .addActionRow(createTextInput("description", "Description", TextInputStyle.PARAGRAPH, false, 4000, null, embed.getDescription()).build())
                    .build();
            case "field" -> Modals.create("Field", MODAL_FIELD)
                    .setTimeout(10, TimeUnit.MINUTES, () -> {})
                    .addActionRow(createTextInput("fieldName", "Name", TextInputStyle.SHORT, true, MessageEmbed.TITLE_MAX_LENGTH, null, null).build())
                    .addActionRow(createTextInput("fieldValue", "Value", TextInputStyle.PARAGRAPH, true, MessageEmbed.VALUE_MAX_LENGTH, null, null).build())
                    .addActionRow(createTextInput("fieldInline", "Inline", TextInputStyle.SHORT, false, 5, "true/false", "false").build())
                    .build();
            case "media" -> {
                final MessageEmbed.Thumbnail thumbnail = embed.getThumbnail();
                final MessageEmbed.ImageInfo image = embed.getImage();
                yield Modals.create("Media", MODAL_MEDIA)
                        .setTimeout(10, TimeUnit.MINUTES, () -> {})
                        .addActionRow(createTextInput("thumbnail", "Thumbnail", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", thumbnail == null ? null : thumbnail.getUrl()).build())
                        .addActionRow(createTextInput("image", "Image", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", image == null ? null : image.getUrl()).build())
                        .build();
            }
            case "footer" -> {
                final MessageEmbed.Footer footer = embed.getFooter();
                final TemporalAccessor timestamp = embed.getTimestamp();
                yield Modals.create("Footer", MODAL_FOOTER)
                        .setTimeout(10, TimeUnit.MINUTES, () -> {})
                        .addActionRow(createTextInput("footerText", "Footer text", TextInputStyle.SHORT, false, MessageEmbed.TEXT_MAX_LENGTH, null, footer == null ? null : footer.getText()).build())
                        .addActionRow(createTextInput("footerIconUrl", "Footer icon", TextInputStyle.SHORT, false, MessageEmbed.URL_MAX_LENGTH, "http:// or https://", footer == null ? null : footer.getIconUrl()).build())
                        .addActionRow(createTextInput("timestamp", "Timestamp", TextInputStyle.SHORT, false, 10, "Epoch time or 'now'", timestamp == null ? null : String.valueOf(Instant.from(timestamp).toEpochMilli())).build())
                        .build();
            }
            default -> null;
        };

        if (modal != null) event.replyModal(modal).queue();
    }

    @ModalHandler(name = MODAL_COLOR)
    public void modalColor(@NotNull ModalInteractionEvent event,
                           @ModalInput(name = "color") @NotNull String color) {
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

    @ModalHandler(name = MODAL_AUTHOR)
    public void modalAuthor(@NotNull ModalInteractionEvent event,
                            @ModalInput(name = "authorName") @NotNull String authorName,
                            @ModalInput(name = "authorUrl") @NotNull String authorUrl,
                            @ModalInput(name = "authorIconUrl") @NotNull String authorIconUrl) {
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

    @ModalHandler(name = MODAL_TITLE)
    public void modalTitle(@NotNull ModalInteractionEvent event,
                           @ModalInput(name = "titleText") @NotNull String titleText,
                           @ModalInput(name = "titleUrl") @NotNull String titleUrl) {
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

    @ModalHandler(name = MODAL_DESCRIPTION)
    public void modalDescription(@NotNull ModalInteractionEvent event,
                                 @ModalInput(name = "description") @NotNull String description) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message != null) editEmbed(event, new EmbedBuilder(message.getEmbeds().getFirst()).setDescription(description));
    }

    @ModalHandler(name = MODAL_FIELD)
    public void modalField(@NotNull ModalInteractionEvent event,
                           @ModalInput(name = "fieldName") @NotNull String fieldName,
                           @ModalInput(name = "fieldValue") @NotNull String fieldValue,
                           @ModalInput(name = "fieldInline") @NotNull String fieldInline) {
        event.deferEdit().queue();
        final Message message = event.getMessage();
        if (message != null) editEmbed(event, new EmbedBuilder(message.getEmbeds().getFirst()).addField(fieldName, fieldValue, Boolean.parseBoolean(fieldInline)));
    }

    @ModalHandler(name = MODAL_MEDIA)
    public void modalMedia(@NotNull ModalInteractionEvent event,
                           @ModalInput(name = "thumbnail") @NotNull String thumbnail,
                           @ModalInput(name = "image") @NotNull String image) {
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

    @ModalHandler(name = MODAL_FOOTER)
    public void modalFooter(@NotNull ModalInteractionEvent event,
                            @ModalInput(name = "footerText") @NotNull String footerText,
                            @ModalInput(name = "footerIconUrl") @NotNull String footerIconUrl,
                            @ModalInput(name = "timestamp") @NotNull String timestamp) {
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
    private TextInput.Builder createTextInput(@NotNull String inputName, @NotNull String label, @NotNull TextInputStyle style, boolean required, int maxLength, @Nullable String placeholder, @Nullable String value) {
        return Modals.createTextInput(inputName, label, style)
                .setRequired(required)
                .setMaxLength(maxLength)
                .setPlaceholder(placeholder)
                .setValue(value);
    }

    @Nullable
    private String processInput(@NotNull String input) {
        return input.isEmpty() || input.equalsIgnoreCase("null") ? null : input;
    }

    private void error(@NotNull ModalInteractionEvent event, @NotNull String parameter, @Nullable String value) {
        event.getHook().editOriginal(LazyEmoji.NO + " **Invalid " + parameter + "**: `" + value + "`").queue();
    }

    private void editEmbed(@NotNull ModalInteractionEvent event, @NotNull EmbedBuilder builder) {
        try {
            event.getHook().editOriginal("").setEmbeds(builder.build()).queue();
        } catch (final IllegalStateException e) {
            error(event, "embed", e.getMessage());
        }
    }
}
