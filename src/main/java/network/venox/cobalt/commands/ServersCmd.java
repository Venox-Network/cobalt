package network.venox.cobalt.commands;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;


public class ServersCmd extends CoExecutableCommand {
    public ServersCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Lists all servers the bot is in";
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        // Generate embed
        final CoEmbed embed = cobalt.messages.getEmbed("command", "list-servers");
        cobalt.jda.getGuilds().forEach(guild -> embed.addField(guild.getName(), "`" + guild.getId() + "`", true));

        // Send embed
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
