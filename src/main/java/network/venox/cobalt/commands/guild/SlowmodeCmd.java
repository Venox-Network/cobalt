package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoSlowmode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@CommandMarker @UserPermissions(Permission.MANAGE_CHANNEL)
public class SlowmodeCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "slowmode",
            description = "Manage the dynamic slowmode of a channel")
    public void slowmodeCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The channel to manage slowmode for") @Nullable TextChannel channel,
                          @AppOption(description = "The minimum slowmode (in seconds)") @LongRange(from = 0, to = ISlowmodeChannel.MAX_SLOWMODE) @Nullable Integer minimum,
                          @AppOption(description = "The maximum slowmode (in seconds)") @LongRange(from = 0, to = ISlowmodeChannel.MAX_SLOWMODE) @Nullable Integer maximum) {
        if (channel == null) channel = event.getChannel().asTextChannel();
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());
        final CoSlowmode current = guild.getSlowmode(channel.getIdLong());

        // Remove slowmode if no minimum or maximum is specified
        if (current != null && minimum == null && maximum == null) {
            guild.slowmodes.remove(current);
            event.reply("Removed dynamic slowmode for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Get minimum
        int minimumValue = 0;
        if (minimum == null) {
            if (current != null) minimumValue = current.minimum;
        } else {
            minimumValue = minimum;
        }

        // Get maximum
        final int maximumValue;
        if (maximum == null) {
            if (current != null) {
                maximumValue = current.maximum;
            } else {
                event.reply("You must specify a maximum slowmode!").setEphemeral(true).queue();
                return;
            }
        } else {
            maximumValue = maximum;
        }

        // Check if minimum is greater than maximum
        if (minimumValue > maximumValue) {
            event.reply("The minimum slowmode cannot be greater than the maximum!").setEphemeral(true).queue();
            return;
        }

        // Check if minimum and maximum are the same
        if (minimumValue == maximumValue) {
            event.reply("The minimum and maximum slowmode cannot be the same!").setEphemeral(true).queue();
            return;
        }

        // Update slowmode
        if (current != null) {
            current.minimum = minimumValue;
            current.maximum = maximumValue;
        } else {
            guild.slowmodes.add(new CoSlowmode(channel.getIdLong(), minimumValue, maximumValue));
        }

        // Reply
        event.reply("Set dynamic slowmode for " + channel.getAsMention() + " to `" + minimumValue + "-" + maximumValue + "` seconds").setEphemeral(true).queue();
    }
}
