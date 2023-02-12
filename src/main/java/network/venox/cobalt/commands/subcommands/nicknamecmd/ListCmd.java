package network.venox.cobalt.commands.subcommands.nicknamecmd;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Set;


public class ListCmd extends CoSubCommand {
    public ListCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "List the current phrases in the nickname blacklist";
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final CoGuild guild = event.getCoGuild();
        if (guild == null) return;
        final Set<String> nicknames = guild.nicknameBlacklist;
        event.reply("**The current blacklist contains `" + nicknames.size() + "` nicknames:**\n`" + String.join("`, `", nicknames) + "`").setEphemeral(true).queue();
    }
}
