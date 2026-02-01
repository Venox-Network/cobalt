package network.venox.cobalt.commands.global.power;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.services.power.BotPower;


@Command
public class PowerStop {
    @NotNull private final CoConfig config;
    @NotNull private final BotPower power;

    public PowerStop(@NotNull CoConfig config, @NotNull BotPower power) {
        this.config = config;
        this.power = power;
    }

    @JDASlashCommand(
            name = "power",
            subcommand = "stop",
            description = "OWNER | Stop the bot in-case of emergency")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        if (config.checkIsOwner(event)) event.reply(LazyEmoji.YES + " Stopping the bot...").setEphemeral(true).queue(_ -> power.gracefulStop());
    }
}
