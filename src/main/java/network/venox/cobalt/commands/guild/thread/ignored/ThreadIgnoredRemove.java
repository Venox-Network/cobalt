package network.venox.cobalt.commands.guild.thread.ignored;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@Command
public class ThreadIgnoredRemove {
    @NotNull private final MongoProvider mongo;

    public ThreadIgnoredRemove(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
    @JDASlashCommand(
            name = "thread",
            group = "ignored",
            subcommand = "remove",
            description = "Remove a phrase/role from the ignored list for an auto-thread channel")
    public void threadIgnoredRemove(@NotNull GuildSlashEvent event,
                                    @SlashOption(description = "The channel to remove the ignored phrase/role from") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                                    @SlashOption(description = "The phrase to remove from the ignored list") @Nullable String phrase,
                                    @SlashOption(description = "The role to remove from the ignored list") @Nullable Role role) {
        if (phrase == null && role == null) {
            event.reply(LazyEmoji.NO + " You must provide a phrase or role to remove from the ignored list!").setEphemeral(true).queue();
            return;
        }
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        final MagicCollection<AutoThread> collection = mongo.database.getMagicCollection(AutoThread.class);

        // Phrase
        if (phrase != null) {
            collection.updateOne(
                    Filters.eq("_id", channel.getIdLong()),
                    Updates.pull(AutoThread.PROP_IGNORED_PHRASES, phrase.toLowerCase().trim()));
            event.reply(LazyEmoji.YES + " `" + phrase + "` has been removed from the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        // Role
        collection.updateOne(
                Filters.eq("_id", channel.getIdLong()),
                Updates.pull(AutoThread.PROP_IGNORED_ROLES, role.getIdLong()));
        event.reply(LazyEmoji.YES + " " + role.getAsMention() + " has been removed from the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
    }
}
