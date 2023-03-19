package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.TtsManager;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class TtsContext extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "TTS")
    public void ttsContext(@NotNull GuildMessageEvent event) {
        TtsManager.speak(event, event.getTarget().getContentRaw());
    }
}
