package network.venox.cobalt.commands.guild;

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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER})
public class WelcomeCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "welcome",
            subcommand = "set",
            description = "Set the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event,
                               @AppOption(description = "The channel to set as welcome channel") @ChannelTypes({ChannelType.TEXT}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        bot.dataManager.mongo.getMagicCollection(Server.class).upsertOne(
                Filters.eq("_id", event.getGuild().getIdLong()),
                Updates.set(Server.PROP_WELCOME_CHANNEL, channel.getIdLong()));
        event.reply(LazyEmoji.YES + " Welcome channel has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            name = "welcome",
            subcommand = "remove",
            description = "Remove the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event) {
        bot.dataManager.mongo.getMagicCollection(Server.class).upsertOne(
                Filters.eq("_id", event.getGuild().getIdLong()),
                Updates.unset(Server.PROP_WELCOME_CHANNEL));
        event.reply(LazyEmoji.YES + " Welcome channel has been removed").setEphemeral(true).queue();
    }
}
