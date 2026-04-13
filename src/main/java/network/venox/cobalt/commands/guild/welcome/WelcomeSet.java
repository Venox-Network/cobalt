package network.venox.cobalt.commands.guild.welcome;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

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
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class WelcomeSet {
    @NotNull private final MongoProvider mongo;

    public WelcomeSet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER})
    @JDASlashCommand(
            name = "welcome",
            subcommand = "set",
            description = "Set the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event,
                               @SlashOption(description = "The channel to set as welcome channel") @ChannelTypes({ChannelType.TEXT}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        mongo.database.getMagicCollection(Server.class).upsertOne(
                Filters.eq("_id", event.getGuild().getIdLong()),
                Updates.set(Server.PROP_WELCOME_CHANNEL, channel.getIdLong()));
        event.reply(LazyEmoji.YES + " Welcome channel has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
