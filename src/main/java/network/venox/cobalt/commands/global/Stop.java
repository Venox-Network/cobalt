package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class Stop extends ApplicationCommand {
    @NotNull private final CoConfig config;

    public Stop(@NotNull CoConfig config) {
        this.config = config;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "stop",
            description = "OWNER | Stop the bot in-case of emergency")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        if (config.checkIsOwner(event)) event.reply(LazyEmoji.YES + " Stopping the bot... *Sometimes it will auto-restart, just run this command again if it does!*").setEphemeral(true).queue(_ -> System.exit(0));
    }
}
