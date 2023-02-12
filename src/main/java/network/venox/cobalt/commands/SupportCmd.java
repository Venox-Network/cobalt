package network.venox.cobalt.commands;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;


public class SupportCmd extends CoExecutableCommand {
    public SupportCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Get the invite to Cobalt's support server";
    }

    @Override
    public boolean ownerOnly() {
        return false;
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final String supportServer = cobalt.config.guildInvite;
        if (supportServer != null) event.reply(supportServer).setEphemeral(true).queue();
    }
}
