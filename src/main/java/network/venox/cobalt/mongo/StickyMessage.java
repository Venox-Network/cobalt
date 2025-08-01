package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class StickyMessage {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MESSAGE = "message";
    @NotNull public static final String PROP_CURRENT = "current";

    /**
     * [channel ID, future]
     */
    @NotNull private static final Map<Long, ScheduledFuture<?>> STICKY_FUTURES = new HashMap<>();

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MESSAGE) public MongoMessage message;
    @BsonProperty(PROP_CURRENT) public long current;

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @NotNull
    public Optional<TextChannel> channel(@NotNull JDA jda) {
        return guild(jda).map(guild -> guild.getTextChannelById(channel));
    }

    public void send(@NotNull Cobalt bot, @NotNull MessageChannel messageChannel) {
        // Delete current message
        delete(messageChannel);

        // Schedule message to be sent
        final ScheduledFuture<?> future = STICKY_FUTURES.get(channel);
        if (future != null) future.cancel(true);
        STICKY_FUTURES.put(channel, MiscUtility.IO_SCHEDULER.schedule(() -> {
            messageChannel.sendMessage(message.toBuilder().build())
                    .queue(msg -> bot.mongo.getMagicCollection(StickyMessage.class).updateOne(
                            Filters.eq("_id", channel),
                            Updates.set(PROP_CURRENT, msg.getIdLong())));
            STICKY_FUTURES.remove(channel);
        }, 1, TimeUnit.MINUTES));
    }

    public void delete(@NotNull MessageChannel textChannel) {
        textChannel.retrieveMessageById(current)
                .flatMap(Message::delete)
                .queue(null, LazyUtilities.IGNORE_UNKNOWN_MESSAGE);
    }

    public static class MongoMessage {
        @NotNull public static final String PROP_CONTENT = "content";
        @NotNull public static final String PROP_EMBEDS = "embeds";

        @BsonProperty(PROP_CONTENT) public String content;
        @BsonProperty(PROP_EMBEDS) @Nullable public List<Embed> embeds;

        public MongoMessage() {}

        public MongoMessage(@NotNull Message message) {
            content = message.getContentRaw();
            final List<MessageEmbed> messageEmbeds = message.getEmbeds();
            if (!messageEmbeds.isEmpty()) {
                embeds = new ArrayList<>();
                for (final MessageEmbed messageEmbed : messageEmbeds) embeds.add(new Embed(messageEmbed));
            }
        }

        @NotNull
        public MessageCreateBuilder toBuilder() {
            final MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.setContent(content);
            builder.setEmbeds(getEmbeds());
            return builder;
        }

        @NotNull
        public List<MessageEmbed> getEmbeds() {
            if (embeds == null) return Collections.emptyList();
            final List<MessageEmbed> list = new ArrayList<>();
            for (final Embed embed : embeds) list.add(embed.build());
            return list;
        }

        public static class Embed {
            @NotNull public static final String PROP_COLOR = "color";
            @NotNull public static final String PROP_AUTHOR = "author";
            @NotNull public static final String PROP_TITLE = "title";
            @NotNull public static final String PROP_DESCRIPTION = "description";
            @NotNull public static final String PROP_THUMBNAIL = "thumbnail";
            @NotNull public static final String PROP_IMAGE = "image";
            @NotNull public static final String PROP_FIELDS = "fields";
            @NotNull public static final String PROP_FOOTER = "footer";
            @NotNull public static final String PROP_TIMESTAMP = "timestamp";

            @BsonProperty(PROP_COLOR) @Nullable public Integer color;
            @BsonProperty(PROP_AUTHOR) @Nullable public Author author;
            @BsonProperty(PROP_TITLE) @Nullable public Title title;
            @BsonProperty(PROP_DESCRIPTION) @Nullable public String description;
            @BsonProperty(PROP_THUMBNAIL) @Nullable public String thumbnail;
            @BsonProperty(PROP_IMAGE) @Nullable public String image;
            @BsonProperty(PROP_FIELDS) @Nullable public List<Field> fields;
            @BsonProperty(PROP_FOOTER) @Nullable public Footer footer;
            @BsonProperty(PROP_TIMESTAMP) @Nullable public Date timestamp;

            public Embed() {}

            public Embed(@NotNull MessageEmbed embed) {
                color = embed.getColorRaw();
                final MessageEmbed.AuthorInfo authorInfo = embed.getAuthor();
                author = authorInfo != null ? new Author(authorInfo) : null;
                final String titleText = embed.getTitle();
                title = titleText != null ? new Title(titleText, embed.getUrl()) : null;
                description = embed.getDescription();
                final MessageEmbed.Thumbnail embedThumbnail = embed.getThumbnail();
                thumbnail = embedThumbnail != null ? embedThumbnail.getUrl() : null;
                final MessageEmbed.ImageInfo imageInfo = embed.getImage();
                image = imageInfo != null ? imageInfo.getUrl() : null;
                fields = new ArrayList<>();
                for (final MessageEmbed.Field field : embed.getFields()) fields.add(new Field(field));
                final MessageEmbed.Footer embedFooter = embed.getFooter();
                footer = embedFooter != null ? new Footer(embedFooter) : null;
                final OffsetDateTime embedTimestamp = embed.getTimestamp();
                timestamp = embedTimestamp != null ? Date.from(embedTimestamp.toInstant()) : null;
            }

            @NotNull
            public MessageEmbed build() {
                final EmbedBuilder builder = new EmbedBuilder();
                if (color != null) builder.setColor(color);
                if (author != null) builder.setAuthor(author.name, author.url, author.icon);
                if (title != null) builder.setTitle(title.text, title.url);
                if (description != null) builder.setDescription(description);
                if (thumbnail != null) builder.setThumbnail(thumbnail);
                if (image != null) builder.setImage(image);
                if (fields != null) for (final Field field : fields) builder.addField(field.name, field.value, field.inline);
                if (footer != null) builder.setFooter(footer.text, footer.icon);
                if (timestamp != null) builder.setTimestamp(timestamp.toInstant());
                return builder.build();
            }

            public static class Author {
                @NotNull public static final String PROP_NAME = "name";
                @NotNull public static final String PROP_URL = "url";
                @NotNull public static final String PROP_ICON = "icon";

                @BsonProperty(PROP_NAME) public String name;
                @BsonProperty(PROP_URL) @Nullable public String url;
                @BsonProperty(PROP_ICON) @Nullable public String icon;

                public Author() {}

                public Author(@NotNull MessageEmbed.AuthorInfo author) {
                    name = author.getName();
                    url = author.getUrl();
                    icon = author.getIconUrl();
                }
            }

            public static class Title {
                @NotNull public static final String PROP_TEXT = "text";
                @NotNull public static final String PROP_URL = "url";

                @BsonProperty(PROP_TEXT) public String text;
                @BsonProperty(PROP_URL) @Nullable public String url;

                public Title() {}

                public Title(@NotNull String text, @Nullable String url) {
                    this.text = text;
                    this.url = url;
                }
            }

            public static class Field {
                @NotNull public static final String PROP_NAME = "name";
                @NotNull public static final String PROP_VALUE = "value";
                @NotNull public static final String PROP_INLINE = "inline";

                @BsonProperty(PROP_NAME) public String name;
                @BsonProperty(PROP_VALUE) public String value;
                @BsonProperty(PROP_INLINE) public boolean inline;

                public Field() {}

                public Field(@NotNull MessageEmbed.Field field) {
                    name = field.getName();
                    value = field.getValue();
                    inline = field.isInline();
                }
            }

            public static class Footer {
                @NotNull public static final String PROP_TEXT = "text";
                @NotNull public static final String PROP_ICON = "icon";

                @BsonProperty(PROP_TEXT) public String text;
                @BsonProperty(PROP_ICON) @Nullable public String icon;

                public Footer() {}

                public Footer(@NotNull MessageEmbed.Footer footer) {
                    text = footer.getText();
                    icon = footer.getIconUrl();
                }
            }
        }
    }
}
