package network.venox.cobalt.commands.guild.thread;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
public class ThreadDisable extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "thread",
            subcommand = "disable",
            description = "Disable auto-threading for a channel")
    public void disableCommand(@NotNull GuildSlashEvent event,
                               @AppOption(description = "The channel to disable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        // Delete thread channel
        if (bot.mongo.getMagicCollection(AutoThread.class).deleteOne(Filters.eq("_id", channel.getIdLong())).getDeletedCount() == 0) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is already disabled!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " Auto-threading for " + channel.getAsMention() + " has been **disabled**").setEphemeral(true).queue();
    }
}
