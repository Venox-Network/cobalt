package network.venox.cobalt.apps;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.TtsManager;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;


@CommandMarker
public class TtsContext extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "TTS")
    public void ttsContext(@NotNull GuildMessageEvent event) {
        if (!LazyUtilities.userHasChannelPermission(event, Permission.MESSAGE_SEND)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }
        TtsManager.speak(event, event.getTarget().getContentRaw());
    }
}
