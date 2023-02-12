package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoSlowmode;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;


public class SlowmodeCmd extends CoExecutableCommand {
    public SlowmodeCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Manage the dynamic slowmode of a channel";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "channel", "The channel to manage slowmode for", false)
                        .setChannelTypes(ChannelType.TEXT),
                new OptionData(OptionType.INTEGER, "minimum", "The minimum slowmode (in seconds)", false, true)
                        .setMinValue(0)
                        .setMaxValue(21600),
                new OptionData(OptionType.INTEGER, "maximum", "The maximum slowmode (in seconds)", false, true)
                        .setMinValue(0)
                        .setMaxValue(21600));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final CoGuild guild = event.getCoGuild();
        if (guild == null) return;
        final OptionMapping channelOption = event.getOption("channel");
        final OptionMapping minimumOption = event.getOption("minimum");
        final OptionMapping maximumOption = event.getOption("maximum");
        final TextChannel channel = channelOption == null ? event.getChannel().asTextChannel() : channelOption.getAsChannel().asTextChannel();
        final CoSlowmode current = guild.getSlowmode(channel.getIdLong());

        // Remove slowmode if no minimum or maximum is specified
        if (current != null && minimumOption == null && maximumOption == null) {
            guild.slowmodes.remove(current);
            event.reply("Removed dynamic slowmode for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Get minimum
        int minimum = 0;
        if (minimumOption == null) {
            if (current != null) minimum = current.minimum;
        } else {
            minimum = minimumOption.getAsInt();
        }

        // Get maximum
        final int maximum;
        if (maximumOption == null) {
            if (current != null) {
                maximum = current.maximum;
            } else {
                event.reply("You must specify a maximum slowmode!").setEphemeral(true).queue();
                return;
            }
        } else {
            maximum = maximumOption.getAsInt();
        }

        // Check if minimum is greater than maximum
        if (minimum > maximum) {
            event.reply("The minimum slowmode cannot be greater than the maximum!").setEphemeral(true).queue();
            return;
        }

        // Check if minimum and maximum are the same
        if (minimum == maximum) {
            event.reply("The minimum and maximum slowmode cannot be the same!").setEphemeral(true).queue();
            return;
        }

        // Update slowmode
        if (current != null) {
            current.minimum = minimum;
            current.maximum = maximum;
        } else {
            guild.slowmodes.add(new CoSlowmode(channel.getIdLong(), minimum, maximum));
        }

        // Reply
        event.reply("Set dynamic slowmode for " + channel.getAsMention() + " to `" + minimum + "-" + maximum + "` seconds").setEphemeral(true).queue();
    }

    @Override @Nullable
    public Set<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        final CoGuild guild = event.getCoGuild();
        final MessageChannelUnion channel = event.getChannel();
        if (guild == null || channel == null) return null;
        final CoSlowmode current = guild.getSlowmode(channel.getIdLong());
        if (current == null) return null;
        final String option = event.getFocusedOption().getName();

        // minimum
        if (option.equals("minimum")) return Set.of(new Command.Choice("Current minimum", current.minimum));

        // maximum
        if (option.equals("maximum")) return Set.of(new Command.Choice("Current maximum", current.maximum));

        return null;
    }
}
