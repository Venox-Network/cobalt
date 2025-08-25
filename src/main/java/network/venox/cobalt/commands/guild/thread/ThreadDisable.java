package network.venox.cobalt.commands.guild.thread;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class ThreadDisable extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public ThreadDisable(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
    @JDASlashCommand(
            name = "thread",
            subcommand = "disable",
            description = "Disable auto-threading for a channel")
    public void disableCommand(@NotNull GuildSlashEvent event,
                               @SlashOption(description = "The channel to disable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Delete thread channel
        if (mongo.database.getMagicCollection(AutoThread.class).deleteOne(Filters.eq("_id", channel.getIdLong())).getDeletedCount() == 0) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is already disabled!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " Auto-threading for " + channel.getAsMention() + " has been **disabled**").setEphemeral(true).queue();
    }
}
