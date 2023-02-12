package network.venox.cobalt.commands.subcommands.reactcmd;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoReactChannel;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class UnsetCmd extends CoSubCommand {
    public UnsetCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Unset a channel as a reaction channel";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.CHANNEL, "channel", "The channel to unset as a reaction channel", true)
                .setChannelTypes(ChannelType.TEXT));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping channelOption = event.getOption("channel");
        final CoGuild guild = event.getCoGuild();
        if (channelOption == null || guild == null) return;
        final TextChannel channel = CoMapper.handleException(() -> channelOption.getAsChannel().asTextChannel());
        if (channel == null) return;

        // Get reaction channel
        final CoReactChannel reactChannel = guild.getReactChannel(channel.getIdLong());
        if (reactChannel == null) {
            event.reply("This channel is not a reaction channel").setEphemeral(true).queue();
            return;
        }

        // Remove reaction channel
        guild.reactChannels.remove(reactChannel);
        event.reply(channel.getAsMention() + " is no longer a reaction channel").setEphemeral(true).queue();
    }
}
