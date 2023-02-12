package network.venox.cobalt.commands;

import net.dv8tion.jda.api.interactions.InteractionHook;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;


public class ReloadCmd extends CoExecutableCommand {
    public ReloadCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Saves the bots data and reloads it";
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        final InteractionHook hook = event.getHook();

        cobalt.data.save();
        hook.editOriginal("Data saved!").queue();
        cobalt.data.load();
        hook.editOriginal("Data loaded!").queue();
    }
}
