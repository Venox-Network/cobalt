package network.venox.cobalt.commands.subcommands.reactcmd;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.ReactCmd;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoReactChannel;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SetCmd extends CoSubCommand {
    public SetCmd(@NotNull ReactCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Sets the emoji(s) for a channel";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Arrays.asList(
                new OptionData(OptionType.CHANNEL, "channel", "The channel to manage", false)
                        .setChannelTypes(ChannelType.TEXT),
                new OptionData(OptionType.STRING, "emojis", "The emojis to set. If empty, channel will be dynamic", false));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final CoGuild guild = event.getCoGuild();
        if (guild == null) return;
        final OptionMapping channelOption = event.getOption("channel");
        final TextChannel channel = CoMapper.handleException(() -> channelOption != null ? channelOption.getAsChannel().asTextChannel() : event.getChannel().asTextChannel());
        if (channel == null) return;
        final OptionMapping emojisOption = event.getOption("emojis");

        // Dynamic
        if (emojisOption == null) {
            final CoReactChannel reactChannel = guild.getReactChannel(channel.getIdLong());
            if (reactChannel != null) {
                reactChannel.emojis = null;
            } else {
                guild.reactChannels.add(new CoReactChannel(channel.getIdLong(), null));
            }
            event.reply(channel.getAsMention() + " has been set as a dynamic react channel").setEphemeral(true).queue();
            return;
        }

        // Static
        final List<String> emojis = new ArrayList<>(Arrays.asList(emojisOption.getAsString().split(" ")));
        if (emojis.size() > 20) emojis.subList(20, emojis.size()).clear();
        final CoReactChannel reactChannel = guild.getReactChannel(channel.getIdLong());
        if (reactChannel != null) {
            reactChannel.emojis = emojis;
        } else {
            guild.reactChannels.add(new CoReactChannel(channel.getIdLong(), emojis));
        }
        event.reply(channel.getAsMention() + " has been set as a static react channel with emojis: " + String.join(" ", emojis)).setEphemeral(true).queue();
    }
}
