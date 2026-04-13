package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class Say {
    @NotNull private final CoConfig config;

    public Say(@NotNull CoConfig config) {
        this.config = config;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "say",
            description = "Make the bot say something")
    public void onCommand(@NotNull GlobalSlashEvent event,
                          @SlashOption(description = "The message to say") @NotNull String message,
                          @SlashOption(description = "The channel to say the message in") @Nullable TextChannel channel) {
        if (!config.checkIsOwner(event)) return;
        final TextChannel currentChannel = event.getChannel().asTextChannel();
        if (channel == null) channel = currentChannel;

        // Send message
        final MessageCreateAction action = channel.sendMessage(message);
        if (channel.getIdLong() != currentChannel.getIdLong()) {
            action.queue(sentMessage -> event.reply(LazyEmoji.YES + " " + sentMessage.getJumpUrl()).setEphemeral(true).queue());
            return;
        }
        action.flatMap(_ -> event.deferReply(true))
                .flatMap(InteractionHook::deleteOriginal)
                .queue();
    }
}
