package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoThreadChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
public class ThreadCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            subcommand = "enable",
            description = "Enabled auto-threading for a channel")
    public void enableCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The channel to enable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                              @AppOption(description = "The name each thread will have") @Nullable String name) {
        if (channel == null) channel = event.getGuildChannel();
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Check if thread channel already exists
        if (guild.getThreadChannel(channel.getIdLong()) != null) {
            event.reply("Auto-threading for " + channel.getAsMention() + " is already enabled").setEphemeral(true).queue();
            return;
        }

        // Add thread channel
        guild.threadChannels.add(new CoThreadChannel(channel.getIdLong(), name, 1, null, null));
        event.reply("Auto-threading for " + channel.getAsMention() + " has been **enabled**").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            subcommand = "disable",
            description = "Disabled auto-threading for a channel")
    public void disableCommand(@NotNull GuildSlashEvent event,
                               @AppOption(description = "The channel to disable auto-threading for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();
        final CoThreadChannel threadChannel = getThreadChannel(event, channel);
        if (threadChannel == null) return;

        // Remove thread channel
        cobalt.data.getGuild(event.getGuild()).threadChannels.remove(threadChannel);
        event.reply("Auto-threading for " + channel.getAsMention() + " has been **disabled**").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            group = "ignored",
            subcommand = "list",
            description = "List the ignored phrases/roles for an auto-thread channel")
    public void ignoredListCommand(@NotNull GuildSlashEvent event,
                                   @AppOption(description = "The channel to list the ignored phrases/roles for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();
        final CoThreadChannel threadChannel = getThreadChannel(event, channel);
        if (threadChannel == null) return;

        // Get ignored phrases/roles
        final Set<String> ignoredPhrases = threadChannel.ignoredPhrases;
        final boolean hasPhrases = !ignoredPhrases.isEmpty();
        final boolean hasRoles = !threadChannel.ignoredRoles.isEmpty();
        if (!hasPhrases && !hasRoles) {
            event.reply("There are no ignored phrases/roles for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        final StringBuilder builder = new StringBuilder();
        if (hasPhrases) {
            builder.append("**Ignored Phrases:** ");
            ignoredPhrases.forEach(phrase -> builder.append("`").append(phrase).append("` "));
        }
        if (hasRoles) {
            builder.append("\n**Ignored Roles:** ");
            threadChannel.getIgnoredRoles(event.getGuild()).forEach(role -> builder.append(role.getAsMention()).append(" "));
        }

        event.reply(builder.toString()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            group = "ignored",
            subcommand = "add",
            description = "Add a phrase/role to the ignored list for an auto-thread channel")
    public void ignoredAddCommand(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The channel to add the ignored phrase/role to") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                                  @AppOption(description = "The phrase to add to the ignored list") @Nullable String phrase,
                                  @AppOption(description = "The role to add to the ignored list") @Nullable Role role) {
        if (phrase == null && role == null) {
            event.reply("You must provide a phrase or role to add to the ignored list").setEphemeral(true).queue();
            return;
        }
        if (channel == null) channel = event.getGuildChannel();
        final CoThreadChannel threadChannel = getThreadChannel(event, channel);
        if (threadChannel == null) return;

        // Phrase
        if (phrase != null) {
            threadChannel.ignoredPhrases.add(phrase.toLowerCase().trim());
            event.reply("`" + phrase + "` has been added to the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        // Role
        threadChannel.ignoredRoles.add(role.getIdLong());
        event.reply(role.getAsMention() + " has been added to the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            group = "ignored",
            subcommand = "remove",
            description = "Remove a phrase/role from the ignored list for an auto-thread channel")
    public void ignoredRemoveCommand(@NotNull GuildSlashEvent event,
                                     @AppOption(description = "The channel to remove the ignored phrase/role from") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel,
                                     @AppOption(description = "The phrase to remove from the ignored list") @Nullable String phrase,
                                     @AppOption(description = "The role to remove from the ignored list") @Nullable Role role) {
        if (phrase == null && role == null) {
            event.reply("You must provide a phrase or role to remove from the ignored list").setEphemeral(true).queue();
            return;
        }
        if (channel == null) channel = event.getGuildChannel();
        final CoThreadChannel threadChannel = getThreadChannel(event, channel);
        if (threadChannel == null) return;

        // Phrase
        if (phrase != null) {
            threadChannel.ignoredPhrases.remove(phrase);
            event.reply("`" + phrase + "` has been removed from the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        // Role
        threadChannel.ignoredRoles.remove(role.getIdLong());
        event.reply(role.getAsMention() + " has been removed from the ignored list for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
    }

    @Nullable
    private CoThreadChannel getThreadChannel(@NotNull GuildSlashEvent event, @NotNull GuildChannel channel) {
        final CoThreadChannel threadChannel = cobalt.data.getGuild(event.getGuild()).getThreadChannel(channel.getIdLong());
        if (threadChannel == null) {
            event.reply("Auto-threading for " + channel.getAsMention() + " is not enabled").setEphemeral(true).queue();
            return null;
        }
        return threadChannel;
    }
}
