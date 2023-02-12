package network.venox.cobalt.commands;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;


public class InviteCmd extends CoExecutableCommand {
    public InviteCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Sends an invite link for the bot";
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        event.replyEmbeds(cobalt.messages.getEmbed("command", "invite")
                        .replace("%invite%", cobalt.jda.getInviteUrl(Permission.ADMINISTRATOR))
                        .build())
                .setEphemeral(true).queue();
    }
}
