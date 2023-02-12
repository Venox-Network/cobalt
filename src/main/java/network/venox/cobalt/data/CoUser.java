package network.venox.cobalt.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.CoFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CoUser {
    @NotNull private final JDA jda;
    public final long userId;
    @NotNull private final CoFile file;

    @NotNull public final Set<String> highlights = new HashSet<>();

    public CoUser(@NotNull JDA jda, long userId) {
        this.jda = jda;
        this.userId = userId;
        this.file = new CoFile("data/users/" + userId, NodeStyle.FLOW, false);

        // Load
        try {
            load();
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    public CoUser(@NotNull User user) {
        this(user.getJDA(), user.getIdLong());
    }

    public void load() throws SerializationException {
        // highlights
        final List<String> highlightList = file.yaml.node("highlights").getList(String.class);
        if (highlightList != null) this.highlights.addAll(highlightList);
    }

    public void save() throws SerializationException {
        // highlights
        final ConfigurationNode highlightsNode = file.yaml.node("highlights");
        highlightsNode.set(null);
        for (final String highlight : highlights) highlightsNode.appendListNode().set(highlight);

        // SAVE FILE
        file.save();
    }

    @Nullable
    public User getUser() {
        return jda.getUserById(userId);
    }

    public void sendHighlight(@NotNull String highlight, @NotNull Message message) {
        final User user = getUser();
        if (user != null) user.openPrivateChannel().queue(channel -> channel.sendMessage("You were highlighted in " + message.getChannel().getAsMention() + " by " + message.getAuthor().getAsMention() + " using `" + highlight + "`:\n> " + message.getJumpUrl()).queue());
    }
}
