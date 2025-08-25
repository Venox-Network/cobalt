package network.venox.cobalt.apps.message;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.TtsManager;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;


@Command
public class TtsApp extends ApplicationCommand {
    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "TTS")
    public void ttsContext(@NotNull GuildMessageEvent event) {
        if (!LazyUtilities.userHasChannelPermission(event, Permission.MESSAGE_SEND)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }
        TtsManager.speak(event, event.getTarget().getContentRaw());
    }
}
