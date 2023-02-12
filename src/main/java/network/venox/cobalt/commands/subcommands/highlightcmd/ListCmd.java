package network.venox.cobalt.commands.subcommands.highlightcmd;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Set;


public class ListCmd extends CoSubCommand {
    public ListCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "List all of your existing highlights";
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final Set<String> highlights = event.getCoUser().highlights;
        if (highlights.isEmpty()) {
            event.reply("You don't have any highlights!").setEphemeral(true).queue();
            return;
        }
        event.reply("`" + String.join("`, `", highlights) + "`").setEphemeral(true).queue();
    }
}
