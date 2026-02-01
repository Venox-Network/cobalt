package network.venox.cobalt.commands.guild.thread;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@Command
public class ThreadEnable {
    @NotNull private final MongoProvider mongo;

    public ThreadEnable(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
    @JDASlashCommand(
            name = "thread",
            subcommand = "enable",
            description = "Enabled auto-threading for a channel")
    public void enableCommand(@NotNull GuildSlashEvent event,
                              @SlashOption(description = "The channel to enable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                              @SlashOption(description = "The name each thread will have") @Nullable String name) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Check if thread channel already exists
        final MagicCollection<AutoThread> collection = mongo.database.getMagicCollection(AutoThread.class);
        if (collection.countDocuments(Filters.eq("_id", channel.getIdLong())) > 0) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is already enabled").setEphemeral(true).queue();
            return;
        }

        // Add thread channel
        collection.insertOne(new AutoThread(channel, name));
        event.reply(LazyEmoji.YES + " Auto-threading for " + channel.getAsMention() + " has been **enabled**").setEphemeral(true).queue();
    }
}
