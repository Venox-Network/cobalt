package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class Stop extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "stop",
            description = "OWNER | Stop the bot in-case of emergency")
    public void onCommand(@NotNull GuildSlashEvent event) {
        if (bot.config.checkIsOwner(event)) event.reply(LazyEmoji.YES + " Stopping the bot... *Sometimes it will auto-restart, just run this command again if it does!*").setEphemeral(true).queue(s -> bot.stopBot());
    }
}
