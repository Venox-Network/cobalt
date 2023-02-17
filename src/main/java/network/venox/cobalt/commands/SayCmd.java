package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@CommandMarker
public class SayCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "say",
            description = "Make the bot say something")
    public void onCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The message to say") @NotNull String message,
                          @AppOption(description = "The channel to say the message in") @Nullable TextChannel channel) {
        if (!cobalt.config.checkIsOwner(event)) return;
        // Get channel
        final TextChannel currentChannel = event.getChannel().asTextChannel();
        if (channel == null) channel = currentChannel;

        // Send message
        final MessageCreateAction action = channel.sendMessage(message);
        if (channel.getIdLong() != currentChannel.getIdLong()) {
            action.queue(sentMessage -> event.reply(sentMessage.getJumpUrl()).setEphemeral(true).queue());
            return;
        }
        action.flatMap(sentMessage -> event.deferReply(true))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();
    }
}
