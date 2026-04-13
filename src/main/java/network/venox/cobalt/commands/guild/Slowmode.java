package network.venox.cobalt.commands.guild;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.AutoSlowmode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@Command
public class Slowmode {
    @NotNull private final MongoProvider mongo;

    public Slowmode(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions(Permission.MANAGE_CHANNEL)
    @JDASlashCommand(
            name = "slowmode",
            description = "Manage the dynamic slowmode of a channel")
    public void slowmodeCommand(@NotNull GuildSlashEvent event,
                                @SlashOption(description = "The channel to manage slowmode for (default: current)") @Nullable TextChannel channel,
                                @SlashOption(description = "The minimum slowmode (in seconds)") @LongRange(from = 1, to = ISlowmodeChannel.MAX_SLOWMODE - 1) @Nullable Integer minimum,
                                @SlashOption(description = "The maximum slowmode (in seconds)") @LongRange(from = 2, to = ISlowmodeChannel.MAX_SLOWMODE) @Nullable Integer maximum) {
        if (channel == null) channel = event.getChannel().asTextChannel();
        final MagicCollection<AutoSlowmode> collection = mongo.database.getMagicCollection(AutoSlowmode.class);
        final AutoSlowmode current = collection.findOne("_id", channel.getIdLong()).orElse(null);

        // Remove slowmode if no minimum or maximum is specified
        if (current != null && minimum == null && maximum == null) {
            collection.deleteOne("_id", channel.getIdLong());
            event.reply(LazyEmoji.YES + " Removed dynamic slowmode for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Get minimum
        int minimumValue = 1;
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
