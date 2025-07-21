package network.venox.cobalt.commands.guild.thread.ignored;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
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
public class ThreadIgnoredAdd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "thread",
            group = "ignored",
            subcommand = "add",
            description = "Add a phrase/role to the ignored list for an auto-thread channel")
    public void ignoredAddCommand(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The channel to add the ignored phrase/role to") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                                  @AppOption(description = "The phrase to add to the ignored list") @Nullable String phrase,
                                  @AppOption(description = "The role to add to the ignored list") @Nullable Role role) {
        if (phrase == null && role == null) {
            event.reply(LazyEmoji.NO + " You must provide a phrase or role to add to the ignored list!").setEphemeral(true).queue();
            return;
        }
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        final MagicCollection<AutoThread> collection = bot.mongo.getMagicCollection(AutoThread.class);

        // Phrase
        if (phrase != null) {
            collection.updateOne(
                    Filters.eq("_id", channel.getIdLong()),
                    Updates.addToSet(AutoThread.PROP_IGNORED_PHRASES, phrase.toLowerCase().trim()));
            event.reply(LazyEmoji.YES + " `" + phrase + "` has been added to the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        // Role
        collection.updateOne(
                Filters.eq("_id", channel.getIdLong()),
                Updates.addToSet(AutoThread.PROP_IGNORED_ROLES, role.getIdLong()));
        event.reply(LazyEmoji.YES + " " + role.getAsMention() + " has been added to the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
    }
}
