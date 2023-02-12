package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.data.CoGuild;

import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class ThreadCmd extends CoExecutableCommand {
    public ThreadCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Commands to manage the current guild's auto-thread";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.CHANNEL, "channel", "The channel to set the thread to", false)
                .setChannelTypes(ChannelType.TEXT));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final CoGuild coGuild = event.getCoGuild();
        if (coGuild == null) return;
        final OptionMapping channelOption = event.getOption("channel");

        // Current channel
        if (channelOption == null) {
            final Channel channel = event.getChannel();
            event.reply("Auto-threading for " + channel.getAsMention() + " has been **" + (coGuild.toggleThreadChannel(channel.getIdLong()) ? "enabled" : "disabled") + "**").setEphemeral(true).queue();
            return;
        }

        // Provided channel
        final Channel channel = channelOption.getAsChannel();
        event.reply("Auto-threading for " + channel.getAsMention() + " has been **" + (coGuild.toggleThreadChannel(channel.getIdLong()) ? "enabled" : "disabled") + "**").setEphemeral(true).queue();
    }
}
