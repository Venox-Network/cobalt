package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("UnusedReturnValue")
public class CoEmbed implements CoObject {
    @NotNull private final EmbedBuilder embed = new EmbedBuilder();
    @NotNull private final Type type;
    @NotNull private final Map<String, String> replacements = new HashMap<>();

    private int color;
    @Nullable private String authorName;
    @Nullable private String authorUrl;
    @Nullable private String authorIcon;
    @Nullable private String titleText;
    @Nullable private String titleUrl;
    @Nullable private String description;
    @Nullable private String thumbnail;
    @Nullable private String image;
    @NotNull private final List<MessageEmbed.Field> fields = new ArrayList<>();
    @Nullable private String footerText;
    @Nullable private String footerIcon;
    @Nullable private TemporalAccessor timestamp;

    public CoEmbed() {
        this(Type.INFO);
    }

    public CoEmbed(@NotNull ConfigurationNode node) {
        this(Type.getType(node.node("type").getString()));
        if (node.empty()) return;

        // Get some values/nodes
        final int colorValue = node.node("color").getInt();
        final ConfigurationNode authorNode = node.node("author");
        final ConfigurationNode titleNode = node.node("title");
        final ConfigurationNode fieldsNode = node.node("fields");
        final ConfigurationNode footerNode = node.node("footer");
        final long timestampValue = node.node("timestamp").getLong();

        // Set the values
        if (colorValue != 0) setColor(colorValue);
        setAuthor(authorNode.node("name").getString(), authorNode.node("url").getString(), authorNode.node("icon").getString());
        setTitle(titleNode.node("text").getString(), titleNode.node("url").getString());
        setDescription(node.node("description").getString());
        if (!fieldsNode.empty()) for (final ConfigurationNode field : fieldsNode.childrenList()) {
            final String name = field.node("name").getString();
            final String value = field.node("value").getString();
            if (name != null && value != null) addField(name, value, field.node("inline").getBoolean());
        }
        setThumbnail(node.node("thumbnail").getString());
        setImage(node.node("image").getString());
        setFooter(footerNode.node("text").getString(), footerNode.node("icon").getString());
        if (timestampValue != 0) setTimestamp(Instant.ofEpochMilli(timestampValue));
    }

    public CoEmbed(@NotNull MessageEmbed msgEmbed) {
        this((Type) null);

        // Get some values
        final MessageEmbed.AuthorInfo newAuthor = msgEmbed.getAuthor();
        final MessageEmbed.Thumbnail newThumbnail = msgEmbed.getThumbnail();
        final MessageEmbed.ImageInfo newImage = msgEmbed.getImage();
        final MessageEmbed.Footer newFooter = msgEmbed.getFooter();

        setColor(msgEmbed.getColor());
        if (newAuthor != null) setAuthor(newAuthor.getName(), newAuthor.getUrl(), newAuthor.getIconUrl());
        setTitle(msgEmbed.getTitle(), msgEmbed.getUrl());
        setDescription(msgEmbed.getDescription());
        for (final MessageEmbed.Field field : msgEmbed.getFields()) {
            final String name = field.getName();
            final String value = field.getValue();
            if (name != null && value != null) addField(name, value, field.isInline());
        }
        if (newThumbnail != null) setThumbnail(newThumbnail.getUrl());
        if (newImage != null) setImage(newImage.getUrl());
        if (newFooter != null) setFooter(newFooter.getText(), newFooter.getIconUrl());
        setTimestamp(msgEmbed.getTimestamp());
    }

    public CoEmbed(@Nullable Type type) {
        if (type == null) type = Type.INFO;
        this.type = type;
        final String typeTitle = type.getTitle();
        if (typeTitle != null) replacements.put("%type%", typeTitle);
        setColor(null);
        setFooter(null, null);
    }

    @NotNull
    public CoEmbed replace(@NotNull String key, @Nullable Object value) {
        replacements.put(key, String.valueOf(value));
        return this;
    }

    @NotNull
    public MessageEmbed build() {
        // Get replaceable values
        String authorNameReplace = authorName;
        String titleTextReplace = titleText;
        String descriptionReplace = description;
        String footerTextReplace = footerText;

        // Parse replacements for the values
        for (final Map.Entry<String, String> entry : replacements.entrySet()) {
            if (authorNameReplace != null) authorNameReplace = authorNameReplace.replace(entry.getKey(), entry.getValue());
            if (titleTextReplace != null) titleTextReplace = titleTextReplace.replace(entry.getKey(), entry.getValue());
            if (descriptionReplace != null) descriptionReplace = descriptionReplace.replace(entry.getKey(), entry.getValue());
            if (footerTextReplace != null) footerTextReplace = footerTextReplace.replace(entry.getKey(), entry.getValue());
        }

        // Set the values
        setAuthor(authorNameReplace, authorUrl, authorIcon);
        setTitle(titleTextReplace, titleUrl);
        setDescription(descriptionReplace);
        setFooter(footerTextReplace, footerIcon);

        // Fields
        final List<MessageEmbed.Field> fieldsCopy = new ArrayList<>(fields);
        clearFields();
        for (final MessageEmbed.Field field : fieldsCopy) {
            // Get name and value
            String name = field.getName();
            String value = field.getValue();

            // Parse replacements for the name and value
            for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                if (name != null) name = name.replace(entry.getKey(), entry.getValue());
                if (value != null) value = value.replace(entry.getKey(), entry.getValue());
            }

            // Add the new field
            if (name != null && value != null) addField(name, value, field.isInline());
        }

        return embed.build();
    }

    @NotNull
    public CoEmbed setColor(@Nullable Color color) {
        if (color == null) color = type.getColor();
        return setColor(color.getRGB());
    }

    @NotNull
    public CoEmbed setColor(int color) {
        embed.setColor(color);
        this.color = color;
        return this;
    }

    @NotNull
    public CoEmbed setAuthor(@Nullable String name) {
        return setAuthor(name, null, null);
    }

    @NotNull
    public CoEmbed setAuthor(@Nullable String name, @Nullable String url) {
        return setAuthor(name, url, null);
    }

    @NotNull
    public CoEmbed setAuthor(@Nullable String name, @Nullable String url, @Nullable String iconUrl) {
        embed.setAuthor(name, url, iconUrl);
        authorName = name;
        authorUrl = url;
        authorIcon = iconUrl;
        return this;
    }

    @NotNull
    public CoEmbed setTitle(@Nullable String text) {
        return setTitle(text, null);
    }

    @NotNull
    public CoEmbed setTitle(@Nullable String text, @Nullable String url) {
        embed.setTitle(text, url);
        this.titleText = text;
        this.titleUrl = url;
        return this;
    }

    @NotNull
    public CoEmbed setDescription(@Nullable String description) {
        embed.setDescription(description);
        this.description = description;
        return this;
    }

    @NotNull
    public CoEmbed setThumbnail(@Nullable String url) {
        embed.setThumbnail(url);
        this.thumbnail = url;
        return this;
    }

    @NotNull
    public CoEmbed setImage(@Nullable String url) {
        embed.setImage(url);
        this.image = url;
        return this;
    }

    @NotNull
    public CoEmbed addField(@NotNull String name, @NotNull String value, boolean inline) {
        final MessageEmbed.Field field = new MessageEmbed.Field(name, value, inline);
        embed.addField(field);
        fields.add(field);
        return this;
    }

    @NotNull
    public CoEmbed clearFields() {
        embed.clearFields();
        fields.clear();
        return this;
    }

    @NotNull
    public CoEmbed setFooter(@Nullable String text, @Nullable String iconUrl) {
        if (text == null) text = "Cobalt";
        if (iconUrl == null) iconUrl = "https://us-east-1.tixte.net/uploads/cdn.venox.network/zoomed.png";
        embed.setFooter(text, iconUrl);
        this.footerText = text;
        this.footerIcon = iconUrl;
        return this;
    }

    @NotNull
    public CoEmbed setTimestamp(@Nullable TemporalAccessor timestamp) {
        embed.setTimestamp(timestamp);
        this.timestamp = timestamp;
        return this;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        map.put("color", color);
        final HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("name", authorName);
        authorMap.put("url", authorUrl);
        authorMap.put("icon", authorIcon);
        map.put("author", authorMap);
        final HashMap<String, String> titleMap = new HashMap<>();
        titleMap.put("text", titleText);
        titleMap.put("url", titleUrl);
        map.put("title", titleMap);
        map.put("description", description);
        map.put("fields", fields.stream()
                .map(field -> {
                    final Map<String, Object> fieldMap = new HashMap<>();
                    fieldMap.put("name", field.getName());
                    fieldMap.put("value", field.getValue());
                    fieldMap.put("inline", field.isInline());
                    return fieldMap;
                })
                .toList());
        map.put("thumbnail", thumbnail);
        map.put("image", image);
        final HashMap<String, String> footerMap = new HashMap<>();
        footerMap.put("text", footerText);
        footerMap.put("icon", footerIcon);
        map.put("footer", footerMap);
        if (timestamp != null) map.put("timestamp", timestamp.getLong(ChronoField.INSTANT_SECONDS) * 1000 + timestamp.getLong(ChronoField.MILLI_OF_SECOND));
        return map;
    }

    // Pre-built embeds
    @NotNull public static final CoEmbed NO_PERMISSION = new CoEmbed(Type.ERROR)
            .setTitle("No permission!")
            .setDescription("You don't have permission to do that!");

    @NotNull
    public static CoEmbed invalidArgument(@NotNull Object argument) {
        return new CoEmbed(Type.ERROR)
                .setTitle("Invalid argument!")
                .setDescription("The argument `" + argument + "` is invalid!");
    }

    public enum Type {
        INFO("INFO | ", Color.CYAN),
        SUCCESS("SUCCESS | ", Color.GREEN),
        ERROR("ERROR | ", Color.RED),
        WARNING("WARNING | ", Color.ORANGE),
        NONE(null, Color.CYAN);

        @Nullable private final String title;
        @NotNull private final Color color;

        Type(@Nullable String title, @NotNull Color color) {
            this.title = title;
            this.color = color;
        }

        @Nullable @Contract(pure = true)
        public String getTitle() {
            return title;
        }

        @NotNull @Contract(pure = true)
        public Color getColor() {
            return color;
        }

        @Nullable @Contract("null -> null")
        public static Type getType(@Nullable String name) {
            if (name == null) return null;
            return CoMapper.handleException(() -> Type.valueOf(name.toUpperCase()));
        }
    }
}
