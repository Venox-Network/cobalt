package network.venox.cobalt.commands.global.highlight;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.Set;


@CommandMarker
public class HighlightList extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "highlight",
            subcommand = "list",
            description = "List all of your existing highlights")
    public void listCommand(@NotNull GlobalSlashEvent event) {
        final Set<String> highlights = bot.mongo.getMagicCollection(CoUser.class)
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
