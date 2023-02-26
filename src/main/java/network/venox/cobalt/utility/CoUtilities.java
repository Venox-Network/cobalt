package network.venox.cobalt.utility;

import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteAlgorithms;

import com.google.gson.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import network.venox.cobalt.data.objects.CoEmbed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class CoUtilities {
    @Nullable
    public static UserSnowflake getUserSnowflake(@NotNull GlobalSlashEvent event, @NotNull String user) {
        final UserSnowflake snowflake = CoMapper.toUserSnowflake(user);
        if (snowflake == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(user).build()).setEphemeral(true).queue();
            return null;
        }
        return snowflake;
    }

    @NotNull
    public static String formatBoolean(boolean bool, @NotNull String enabled, @NotNull String disabled) {
        return bool ? enabled : disabled;
    }

    @NotNull
    public static String formatBoolean(boolean bool) {
        return formatBoolean(bool, "enabled", "disabled");
    }

    @NotNull
    public static List<Command.Choice> sortChoicesFuzzy(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull Collection<Command.Choice> collection) {
        final OptionType type = event.getFocusedOption().getType();
        return AutocompleteAlgorithms.fuzzyMatching(collection, Command.Choice::getName, event).stream()
                .map(result -> {
                    final Command.Choice choice = result.getReferent();
                    return getChoice(type, choice.getName(), choice.getAsString());
                })
                .toList();
    }
    
    @Nullable
    private static Command.Choice getChoice(@NotNull OptionType type, @NotNull String name, @NotNull String value) {
		return switch (type) {
			case STRING -> new Command.Choice(name, value);
			case INTEGER -> {
                final Long valueLong = CoMapper.toLong(value);
                yield valueLong == null ? null : new Command.Choice(name, valueLong);
			}
			case NUMBER -> {
                final Double valueDouble = CoMapper.toDouble(value);
                yield valueDouble == null ? null : new Command.Choice(name, valueDouble);
			}
			default -> throw new IllegalArgumentException("Invalid autocompletion option type: " + type);
		};
    }

    public static List<Command.Choice> acGuildMembers(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Guild guild = event.getGuild();
        if (guild == null) return List.of();
        return sortChoicesFuzzy(event, guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList());
    }

    public static void dynamicReact(@NotNull Message message) {
        for (final String emoji : message.getContentRaw().split("\\s+")) message.addReaction(Emoji.fromFormatted(emoji)).queue(s -> {}, f -> {});
    }

    /**
     * Converts JSON to an {@link EmbedBuilder}
     * <p><i>Only if the JSON was generated from {@link MessageEmbed#toData()}</i>
     *
     * @param   json    the JSON to convert
     *
     * @return          the {@link EmbedBuilder} or {@code null} if the JSON is invalid
     */
    @Nullable
    public static EmbedBuilder getEmbedFromJson(@NotNull String json) {
        final JsonElement element = JsonParser.parseString(json);
        if (!element.isJsonObject()) return null;
        final JsonObject object = element.getAsJsonObject();
        final EmbedBuilder builder = new EmbedBuilder();

        // Color
        final JsonPrimitive color = object.getAsJsonPrimitive("color");
        if (color != null) builder.setColor(color.getAsInt());

        // Author
        final JsonObject author = object.getAsJsonObject("author");
        if (author != null) {
            final JsonPrimitive name = author.getAsJsonPrimitive("name");
            final JsonPrimitive url = author.getAsJsonPrimitive("url");
            final JsonPrimitive iconUrl = author.getAsJsonPrimitive("icon_url");
            builder.setAuthor(name == null ? null : name.getAsString(), url == null ? null : url.getAsString(), iconUrl == null ? null : iconUrl.getAsString());
        }

        // Title
        final JsonPrimitive title = object.getAsJsonPrimitive("title");
        final JsonPrimitive url = object.getAsJsonPrimitive("url");
        builder.setTitle(title == null ? null : title.getAsString(), url == null ? null : url.getAsString());

        // Description
        final JsonPrimitive description = object.getAsJsonPrimitive("description");
        builder.setDescription(description == null ? null : description.getAsString());

        // Fields
        final JsonArray fields = object.getAsJsonArray("fields");
        if (fields != null) for (final JsonElement field : fields) {
            if (!field.isJsonObject()) continue;
            final JsonObject fieldObject = field.getAsJsonObject();
            final JsonPrimitive name = fieldObject.getAsJsonPrimitive("name");
            final JsonPrimitive value = fieldObject.getAsJsonPrimitive("value");
            final JsonPrimitive inline = fieldObject.getAsJsonPrimitive("inline");
            if (name != null && value != null && inline != null) builder.addField(name.getAsString(), value.getAsString(), inline.getAsBoolean());
        }

        // Thumbnail
        final JsonObject thumbnail = object.getAsJsonObject("thumbnail");
        if (thumbnail != null) {
            final JsonPrimitive thumbnailUrl = thumbnail.getAsJsonPrimitive("url");
            builder.setThumbnail(thumbnailUrl == null ? null : thumbnailUrl.getAsString());
        }

        // Image
        final JsonObject image = object.getAsJsonObject("image");
        if (image != null) {
            final JsonPrimitive imageUrl = image.getAsJsonPrimitive("url");
            builder.setImage(imageUrl == null ? null : imageUrl.getAsString());
        }

        // Footer
        final JsonObject footer = object.getAsJsonObject("footer");
        if (footer != null) {
            final JsonPrimitive footerText = footer.getAsJsonPrimitive("text");
            final JsonPrimitive footerIconUrl = footer.getAsJsonPrimitive("icon_url");
            builder.setFooter(footerText == null ? null : footerText.getAsString(), footerIconUrl == null ? null : footerIconUrl.getAsString());
        }

        // Timestamp
        final JsonPrimitive timestamp = object.getAsJsonPrimitive("timestamp");
        if (timestamp != null) builder.setTimestamp(Instant.ofEpochMilli(timestamp.getAsLong()));

        return builder;
    }

    private CoUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
