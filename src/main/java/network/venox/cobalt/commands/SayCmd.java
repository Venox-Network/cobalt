package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SayCmd extends CoExecutableCommand {
    public SayCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Make the bot say something";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "message", "The message to say", true),
                new OptionData(OptionType.CHANNEL, "channel", "The channel to say the message in", false)
                        .setChannelTypes(ChannelType.TEXT));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping messageOption = event.getOption("message");
        if (messageOption == null) return;
        final OptionMapping channelOption = event.getOption("channel");
        final MessageChannelUnion currentChannel = event.getChannel();

        // Get channel
        TextChannel channel = currentChannel.asTextChannel();
        if (channelOption != null) channel = channelOption.getAsChannel().asTextChannel();

        // Send message
        final String jumpUrl = channel.sendMessage(messageOption.getAsString()).complete().getJumpUrl();

        // Reply
        if (channel != currentChannel) event.reply(jumpUrl).setEphemeral(true).queue();
    }
}
