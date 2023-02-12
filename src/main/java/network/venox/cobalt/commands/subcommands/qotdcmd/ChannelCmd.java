package network.venox.cobalt.commands.subcommands.qotdcmd;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.QotdCmd;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class ChannelCmd extends CoSubCommand {
    public ChannelCmd(@NotNull QotdCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Sets the channel for the global QOTD to be sent in";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.CHANNEL, "channel", "The channel to send the QOTD in. Leave empty to remove QOTD")
                .setChannelTypes(ChannelType.TEXT));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = event.getCoGuild();
        if (guild == null || coGuild == null) return;
        final OptionMapping channelOption = event.getOption("channel");

        // Remove the QOTD channel
        if (channelOption == null) {
            coGuild.qotdChannel = null;
            event.reply("The QOTD channel for `" + guild.getName() + "` has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTD channel
        final Channel channel = channelOption.getAsChannel();
        coGuild.qotdChannel = channel.getIdLong();
        event.reply("The QOTD channel for `" + guild.getName() + "` has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
