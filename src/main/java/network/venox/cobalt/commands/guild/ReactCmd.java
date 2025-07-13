package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.ReactChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.Arrays;
import java.util.List;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION})
public class ReactCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "react",
            subcommand = "set",
            description = "Sets the emoji(s) for a channel")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The channel to manage") @Nullable TextChannel channel,
                           @AppOption(description = "The emojis to set. If empty, channel will be dynamic") @Nullable String emojis) {
        if (channel == null) {
            channel = MiscUtility.handleException(() -> event.getChannel().asTextChannel()).orElse(null);
            if (channel == null) {
                event.reply(LazyEmoji.NO + " Please specify a channel to set!").setEphemeral(true).queue();
                return;
            }
        }
        final MagicCollection<ReactChannel> collection = bot.dataManager.mongo.getMagicCollection(ReactChannel.class);

        // Dynamic
        if (emojis == null) {
            collection.upsertOne(
                    Filters.and(
                            Filters.eq("_id", channel.getIdLong()),
                            Filters.eq(ReactChannel.PROP_GUILD, event.getGuild().getIdLong())),
                    Updates.unset(ReactChannel.PROP_EMOJIS));
            event.reply(LazyEmoji.YES + " " + channel.getAsMention() + " has been set as a dynamic react channel").setEphemeral(true).queue();
            return;
        }

        // Static
        final List<String> emojiList = Arrays.asList(emojis.split(" "));
        if (emojiList.size() > 20) emojiList.subList(20, emojiList.size()).clear();
        collection.upsertOne(
                Filters.and(
                        Filters.eq("_id", channel.getIdLong()),
                        Filters.eq(ReactChannel.PROP_GUILD, event.getGuild().getIdLong())),
                Updates.set(ReactChannel.PROP_EMOJIS, emojiList));
        event.reply(LazyEmoji.YES + " " + channel.getAsMention() + " has been set as a static react channel with emojis: " + String.join(" ", emojiList)).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "react",
            subcommand = "unset",
            description = "Unset a channel as a reaction channel")
    public void unsetCommand(@NotNull GuildSlashEvent event,
                             @AppOption(description = "The channel to unset as a reaction channel") @Nullable TextChannel channel) {
        if (!bot.config.checkIsOwner(event)) return;
        if (channel == null) {
            channel = MiscUtility.handleException(() -> event.getChannel().asTextChannel()).orElse(null);
            if (channel == null) {
                event.reply(LazyEmoji.NO + " Please specify a channel to unset!").setEphemeral(true).queue();
                return;
            }
        }

        // Delete reaction channel
        final ReactChannel reactChannel = bot.dataManager.mongo.getMagicCollection(ReactChannel.class).findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
        if (reactChannel == null) {
            event.reply(LazyEmoji.NO + " This channel is not a reaction channel").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " " + channel.getAsMention() + " is no longer a reaction channel").setEphemeral(true).queue();
    }
}
