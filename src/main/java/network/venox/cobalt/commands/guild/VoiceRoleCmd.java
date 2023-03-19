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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL})
public class VoiceRoleCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "voicerole",
            subcommand = "list",
            description = "List all voice channel and role connections")
    public void listCommand(@NotNull GuildSlashEvent event) {
        final Guild guild = event.getGuild();
        final StringBuilder builder = new StringBuilder();
        cobalt.data.getGuild(guild).voiceRoles.forEach((channelId, roles) -> {
            final AudioChannel channel = guild.getChannelById(AudioChannel.class, channelId);
            if (channel == null) return;
            builder.append(channel.getAsMention()).append(": ");
            roles.forEach(roleId -> {
                final Role role = guild.getRoleById(roleId);
                if (role != null) builder.append(role.getAsMention()).append(", ");
            });
            builder.delete(builder.length() - 2, builder.length());
            builder.append("\n");
        });
        event.reply(builder.toString()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "voicerole",
            subcommand = "add",
            description = "Add a new voice channel and role connection")
    public void addCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The voice channel to connect to the role") @ChannelTypes({ChannelType.VOICE, ChannelType.STAGE}) @NotNull GuildChannel channel,
                           @AppOption(description = "The role to connect to the voice channel") @NotNull Role role) {
        final Map<Long, Set<Long>> voiceRoles = cobalt.data.getGuild(event.getGuild()).voiceRoles;
        final Set<Long> roles = voiceRoles.get(channel.getIdLong());
        if (roles != null && roles.contains(role.getIdLong())) {
            event.reply("This role is already connected to this voice channel").setEphemeral(true).queue();
            return;
        }
        voiceRoles.computeIfAbsent(channel.getIdLong(), k -> new HashSet<>()).add(role.getIdLong());
        event.reply("Connected " + role.getAsMention() + " to " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "voicerole",
            subcommand = "remove",
            description = "Remove a voice channel and role connection")
    public void removeCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The voice channel to disconnect from the role") @ChannelTypes({ChannelType.VOICE, ChannelType.STAGE}) @NotNull GuildChannel channel,
                              @AppOption(description = "The role to disconnect from the voice channel") @NotNull Role role) {
        final Set<Long> roles = cobalt.data.getGuild(event.getGuild()).voiceRoles.get(channel.getIdLong());
        if (roles == null || !roles.remove(role.getIdLong())) {
            event.reply("This role is not connected to this voice channel").setEphemeral(true).queue();
            return;
        }
        event.reply("Disconnected " + role.getAsMention() + " from " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
