package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.MessageEmbed;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class HelpCmd extends CoExecutableCommand {
    @Nullable private MessageEmbed embed;

    public HelpCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Shows this help message";
    }

    @Override
    public boolean ownerOnly() {
        return false;
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        // Create embed if not already created
        if (embed == null) {
            final CoEmbed coEmbed = cobalt.messages.getEmbed("command", "help");
            cobalt.executableCommands.forEach(command -> coEmbed.addField(command.toString(), command.description(), true));
            embed = coEmbed.build();
        }

        // Send embed
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
