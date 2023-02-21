package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class InviteCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "invite",
            description = "Sends an invite link for the bot")
    public void inviteCommand(@NotNull GlobalSlashEvent event) {
        if (!cobalt.config.checkIsOwner(event)) return;
        event.replyEmbeds(cobalt.messages.getEmbed("command", "invite")
                        .replace("%invite%", cobalt.jda.getInviteUrl(Permission.ADMINISTRATOR))
                        .build())
                .setEphemeral(true).queue();
    }
}
