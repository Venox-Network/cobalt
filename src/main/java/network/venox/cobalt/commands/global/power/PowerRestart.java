package network.venox.cobalt.commands.global.power;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.services.power.BotPower;


@Command
public class PowerRestart {
    @NotNull private final CoConfig config;
    @NotNull private final BotPower power;

    public PowerRestart(@NotNull CoConfig config, @NotNull BotPower power) {
        this.config = config;
        this.power = power;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "power",
            subcommand = "restart",
            description = "OWNER | Restart the bot")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        if (config.checkIsOwner(event)) event.reply(LazyEmoji.YES + " Restarting the bot...").setEphemeral(true).queue(_ -> power.gracefulRestart());
    }
}
