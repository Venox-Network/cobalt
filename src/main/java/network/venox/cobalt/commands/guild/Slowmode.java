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

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.AutoSlowmode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@CommandMarker @UserPermissions(Permission.MANAGE_CHANNEL)
public class Slowmode extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "slowmode",
            description = "Manage the dynamic slowmode of a channel")
    public void slowmodeCommand(@NotNull GuildSlashEvent event,
                                @AppOption(description = "The channel to manage slowmode for (default: current)") @Nullable TextChannel channel,
                                @AppOption(description = "The minimum slowmode (in seconds)") @LongRange(from = 0, to = ISlowmodeChannel.MAX_SLOWMODE) @Nullable Integer minimum,
                                @AppOption(description = "The maximum slowmode (in seconds)") @LongRange(from = 0, to = ISlowmodeChannel.MAX_SLOWMODE) @Nullable Integer maximum) {
        if (channel == null) channel = event.getChannel().asTextChannel();
        final MagicCollection<AutoSlowmode> collection = bot.mongo.getMagicCollection(AutoSlowmode.class);
        final AutoSlowmode current = collection.findOne("_id", channel.getIdLong()).orElse(null);

        // Remove slowmode if no minimum or maximum is specified
        if (current != null && minimum == null && maximum == null) {
            collection.deleteOne("_id", channel.getIdLong());
            event.reply(LazyEmoji.YES + " Removed dynamic slowmode for " + channel.getAsMention()).setEphemeral(true).queue();
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
                event.reply(LazyEmoji.NO + " You must specify a maximum slowmode!").setEphemeral(true).queue();
                return;
            }
        } else {
            maximumValue = maximum;
        }

        // Check if minimum is greater than maximum
        if (minimumValue > maximumValue) {
            event.reply(LazyEmoji.NO + " The minimum slowmode cannot be greater than the maximum!").setEphemeral(true).queue();
            return;
        }

        // Check if minimum and maximum are the same
        if (minimumValue == maximumValue) {
            event.reply(LazyEmoji.NO + " The minimum and maximum slowmode cannot be the same!").setEphemeral(true).queue();
            return;
        }

        // Update slowmode
        collection.upsertOne(
                Filters.and(
                        Filters.eq("_id", channel.getIdLong()),
                        Filters.eq(AutoSlowmode.PROP_GUILD, event.getGuild().getIdLong())),
                Updates.combine(
                        Updates.set(AutoSlowmode.PROP_MINIMUM, minimumValue),
                        Updates.set(AutoSlowmode.PROP_MAXIMUM, maximumValue)));

        // Reply
        event.reply(LazyEmoji.YES + " Set dynamic slowmode for " + channel.getAsMention() + " to `" + minimumValue + "-" + maximumValue + "` seconds").setEphemeral(true).queue();
    }
}
