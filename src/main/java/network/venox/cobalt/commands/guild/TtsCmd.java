package network.venox.cobalt.commands.guild;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import network.venox.cobalt.TtsManager;

import org.jetbrains.annotations.NotNull;


@Command
public class TtsCmd {
    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand(
            name = "tts",
            description = "Speak in a voice channel using text-to-speech")
    public void ttsCommand(@NotNull GuildSlashEvent event,
                           @SlashOption(description = "The text to speak") @NotNull String text) {
        TtsManager.speak(event, text);
    }
}
