package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoMessage;
import network.venox.cobalt.data.objects.CoStickyMessage;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class StickyCmd extends CoExecutableCommand {
    public StickyCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Sticky a message to keep it as the last message in the current channel";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "message", "The message to sticky. If empty, sticky will be removed", false));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = event.getCoGuild();
        if (guild == null || coGuild == null) return;
        final OptionMapping messageOption = event.getOption("message");
        final TextChannel channel = event.getChannel().asTextChannel();

        // Delete existing sticky message
        if (messageOption == null) {
            final CoStickyMessage stickyMessage = coGuild.getStickyMessage(channel.getIdLong());
            if (stickyMessage != null) {
                stickyMessage.delete(guild);
                coGuild.stickyMessages.remove(stickyMessage);
            }
            event.reply("Sticky message has been removed from " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        final long messageId = messageOption.getAsLong();
        final Message message = channel.retrieveMessageById(messageId).complete();
        if (message == null) {
            event.reply("Message with the ID `" + messageId + "` not found!").setEphemeral(true).queue();
            return;
        }

        // Delete existing sticky message
        final CoStickyMessage current = coGuild.getStickyMessage(channel.getIdLong());
        if (current != null) {
            current.delete(guild);
            coGuild.stickyMessages.remove(current);
        }

        // Set new sticky message
        final CoStickyMessage stickyMessage = new CoStickyMessage(channel.getIdLong(), new CoMessage(message), null);
        coGuild.stickyMessages.add(stickyMessage);
        event.reply(message.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
        stickyMessage.send(cobalt, guild);
    }
}
