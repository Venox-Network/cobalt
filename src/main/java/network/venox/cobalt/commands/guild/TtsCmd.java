package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.TtsManager;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class TtsCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "tts",
            description = "Speak in a voice channel using text-to-speech")
    public void ttsCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The text to speak") @NotNull String text) {
        TtsManager.speak(event, text);
    }
}
