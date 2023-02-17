package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class CoMessage implements CoObject {
    @Nullable public String content;
    @NotNull public final List<CoEmbed> embeds = new ArrayList<>();

    public CoMessage(@NotNull Message message) {
        this(message.getContentRaw(), message.getEmbeds().stream()
                .map(CoEmbed::new)
                .toList());
    }

    public CoMessage(@Nullable String content, @Nullable List<CoEmbed> embeds) {
        if (embeds == null) embeds = Collections.emptyList();
        this.content = content;
        this.embeds.addAll(embeds);
    }

    @NotNull
    public CoMessage setContent(@Nullable String content) {
        this.content = content;
        return this;
    }

    @NotNull
    public CoMessage addEmbed(@NotNull CoEmbed embed) {
        embeds.add(embed);
        return this;
    }

    @NotNull
    public CoMessage addEmbeds(@NotNull List<CoEmbed> embeds) {
        this.embeds.addAll(embeds);
        return this;
    }

    @NotNull
    public List<MessageEmbed> getBuiltEmbeds() {
        return embeds.stream()
                .map(CoEmbed::build)
                .toList();
    }

    @NotNull
    public MessageCreateBuilder toBuilder() {
        final MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.setContent(content);
        builder.setEmbeds(embeds.stream()
                .map(CoEmbed::build)
                .toList());
        return builder;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        if (!embeds.isEmpty()) {
            map.put("embeds", embeds.stream()
                    .map(CoEmbed::toMap)
                    .toList());
        } else {
            map.put("embeds", null);
        }
        return map;
    }
}
