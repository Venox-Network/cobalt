package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class Invite extends ApplicationCommand {
    @NotNull private final CoConfig config;

    public Invite(@NotNull CoConfig config) {
        this.config = config;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "invite",
            description = "Sends an invite link for the bot")
    public void inviteCommand(@NotNull GlobalSlashEvent event) {
        if (config.checkIsOwner(event)) event.reply(LazyEmoji.YES + " " + event.getJDA().getInviteUrl(Permission.ADMINISTRATOR)).setEphemeral(true).queue();
    }
}
