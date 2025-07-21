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

import xyz.srnyx.magicmongo.MagicCollection;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
public class ThreadEnable extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "thread",
            subcommand = "enable",
            description = "Enabled auto-threading for a channel")
    public void enableCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The channel to enable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                              @AppOption(description = "The name each thread will have") @Nullable String name) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        // Check if thread channel already exists
        final MagicCollection<AutoThread> collection = bot.mongo.getMagicCollection(AutoThread.class);
        if (collection.countDocuments(Filters.eq("_id", channel.getIdLong())) > 0) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is already enabled").setEphemeral(true).queue();
            return;
        }

        // Add thread channel
        collection.insertOne(new AutoThread(channel, name));
        event.reply(LazyEmoji.YES + " Auto-threading for " + channel.getAsMention() + " has been **enabled**").setEphemeral(true).queue();
    }
}
