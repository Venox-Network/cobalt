package network.venox.cobalt.commands.guild.react;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.ReactChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.Arrays;
import java.util.List;


@Command
public class ReactSet {
    @NotNull private final MongoProvider mongo;

    public ReactSet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand(
            name = "react",
            subcommand = "set",
            description = "Sets the emoji(s) for a channel")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @SlashOption(description = "The channel to manage") @Nullable GuildMessageChannel channel,
                           @SlashOption(description = "The emojis to set. If empty, channel will be dynamic") @Nullable String emojis) {
        // Get channel
        if (channel == null) {
            channel = MiscUtility.handleException(() -> event.getChannel().asGuildMessageChannel()).orElse(null);
            if (channel == null) {
                event.reply(LazyEmoji.NO + " Please specify a channel to set!").setEphemeral(true).queue();
                return;
            }
        }

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        final MagicCollection<ReactChannel> collection = mongo.database.getMagicCollection(ReactChannel.class);

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
}
