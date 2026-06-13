package network.venox.cobalt.commands.global.highlight;

import com.mongodb.client.model.Filters;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.User;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import java.util.List;
import java.util.Objects;


@Command
public class HighlightStats {
    @NotNull private final LazyLibrary library;
    @NotNull private final MongoProvider mongo;

    public HighlightStats(@NotNull LazyLibrary library, @NotNull MongoProvider mongo) {
        this.library = library;
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "highlight",
            subcommand = "stats",
            description = "Check how many highlights you've received in total")
    public void highlightCount(@NotNull GlobalSlashEvent event,
                               @SlashOption(description = "OWNER | The user to check the stats of") @Nullable User user) {
        final boolean self = user == null;

        // Only bot owners can use user option
        if (!self && library.checkNotOwner(event)) return;

        // Default to self
        if (self) user = event.getUser();

        // Get count
        final int count = mongo.database.getMagicCollection(CoUser.class)
                .findOne("_id", user.getIdLong())
                .map(coUser -> coUser.highlightsSent)
                .orElse(0);

        // Build reply
        final StringBuilder builder = new StringBuilder(LazyEmoji.YES + " ");
        if (self) {
            builder.append("You have");
        } else {
            builder.append(user.getAsMention()).append(" has");
        }
        builder.append(" received **").append(count).append("** total highlight(s) since <t:1781323200:D>");

        // If owner, include total count
        if (library.isOwner(event.getUser().getIdLong())) {
            // Get users with highlights_sent
            final List<CoUser> users = mongo.database.getMagicCollection(CoUser.class).findMany(Filters.exists(CoUser.PROP_HIGHLIGHTS_SENT));

            // Calculate total highlights sent
            final int totalCount = users.stream()
                    .map(coUser -> Objects.requireNonNull(coUser.highlightsSent))
                    .reduce(0, Integer::sum);

            builder.append("\n").append(LazyEmoji.INFO).append(" **").append(totalCount).append("** total highlights have been sent across **").append(users.size()).append("** users");
        }

        // Reply
        event.reply(builder.toString()).setEphemeral(true).queue();
    }
}
