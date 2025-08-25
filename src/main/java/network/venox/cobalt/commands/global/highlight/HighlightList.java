package network.venox.cobalt.commands.global.highlight;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.Set;


@Command
public class HighlightList extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public HighlightList(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "highlight",
            subcommand = "list",
            description = "List all of your existing highlights")
    public void listCommand(@NotNull GlobalSlashEvent event) {
        final Set<String> highlights = mongo.database.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.highlights)
                .orElse(Set.of());
        if (highlights.isEmpty()) {
            event.reply(LazyEmoji.NO + " You don't have any highlights!").setEphemeral(true).queue();
            return;
        }
        event.reply(LazyEmoji.YES + " `" + String.join("`, `", highlights) + "`").setEphemeral(true).queue();
    }
}
