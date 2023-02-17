package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class SupportCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "support",
            description = "Get the invite to Cobalt's support server")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        final String supportServer = cobalt.config.guildInvite;
        if (supportServer != null) event.reply(supportServer).setEphemeral(true).queue();
    }
}
