package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class ReloadCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "reload",
            description = "Saves the bots data and reloads it")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        if (!cobalt.config.checkIsOwner(event)) return;
        cobalt.data.save();
        cobalt.data.load();
        event.reply("Data reloaded!").setEphemeral(true).queue();
    }
}
