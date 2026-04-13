package network.venox.cobalt.components;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent;
import io.github.freya022.botcommands.api.core.annotations.Handler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Corner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Handler
public class CornerComponents {
    @NotNull public static final String BLACKLIST_MENU = "CornerComponents.menu.blacklist";
    @NotNull public static final String LOCK_BUTTON = "CornerComponents.button.lock";
    @NotNull public static final String USERS_ROLES_MENU = "CornerComponents.menu.usersRoles";

    @NotNull private static final Set<Permission> PERMISSIONS = Set.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT);

    @NotNull private final MongoProvider mongo;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus menus;

    public CornerComponents(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus menus) {
        this.mongo = mongo;
        this.buttons = buttons;
        this.menus = menus;
    }

    @JDAButtonListener(LOCK_BUTTON)
    public void toggleLock(@NotNull ButtonEvent event) {
        // Get Corner
        final Corner corner = getCorner(event);
        if (corner == null) return;
        event.deferEdit().queue();

        // Update permissions
        final GuildMessageChannelUnion channel = event.getGuildChannel();
        final boolean previousStatus = Corner.isLocked(channel);
        channel.getPermissionContainer().upsertPermissionOverride(channel.getGuild().getPublicRole())
                .setDenied(previousStatus ? Set.of() : PERMISSIONS)
                .queue();
        
        // Reply
        final String name = channel.getName();
        event.getHook().editOriginal(previousStatus
                        ? LazyEmoji.UNLOCK + " **" + name + "** is now **unlocked**!"
                        : LazyEmoji.LOCK + " **" + name + "** is now **locked**!")
                .setComponents(corner.getComponents(buttons, menus, channel, Corner.Data.locked(!previousStatus)))
                .queue();
    }

    @JDASelectMenuListener(BLACKLIST_MENU)
    public void blacklist(@NotNull EntitySelectEvent event) {
        // Get Corner
        final Corner corner = getCorner(event);
        if (corner == null) return;
        event.deferEdit().queue();

        // Get current blacklisted users
        final List<Long> existing = new ArrayList<>();
        final GuildMessageChannelUnion channel = event.getGuildChannel();
        final IPermissionContainer container = channel.getPermissionContainer();
        final Guild guild = channel.getGuild();
        for (final PermissionOverride permissionOverride : container.getMemberPermissionOverrides()) {
            final long id = permissionOverride.getIdLong();
            if (id != corner.owner && permissionOverride.getDenied().contains(Permission.VIEW_CHANNEL)) existing.add(id);
        }

        // Get added and removed users
        final List<Long> selected = new ArrayList<>(event.getValues().stream()
                .map(IMentionable::getIdLong)
                .toList());
        selected.remove(corner.owner);
        final List<Long> added = new ArrayList<>(selected);
        added.removeAll(existing);
        final List<Long> removed = new ArrayList<>(existing);
        removed.removeAll(selected);

        // Update permissions
        final List<Member> inChannel = channel.asAudioChannel().getMembers();
        for (final long userId : added) guild.retrieveMemberById(userId)
                .queue(member -> {
                    // Update permissions
                    container.upsertPermissionOverride(member).setDenied(PERMISSIONS).queue();
                    // Kick if in channel
                    if (inChannel.contains(member)) guild.kickVoiceMember(member).queue();
                });
        for (final long userId : removed) guild.retrieveMemberById(userId)
                .queue(member -> {
                    final PermissionOverride override = container.getPermissionOverride(member);
                    if (override != null) override.delete().queue();
                });

        // Reply
        event.getHook().editOriginal(LazyEmoji.YES + " Updated blacklist for **" + channel.getName() + "**!")
                .setComponents(corner.getComponents(buttons, menus, channel, Corner.Data.blacklist(selected)))
                .queue();
    }

    @JDASelectMenuListener(USERS_ROLES_MENU)
    public void usersRoles(@NotNull EntitySelectEvent event) {
        // Get Corner
        final Corner corner = getCorner(event);
        if (corner == null) return;
        event.deferEdit().queue();

        // Get current users/roles
        final List<Long> existing = new ArrayList<>();
        final GuildMessageChannelUnion channel = event.getGuildChannel();
        final IPermissionContainer container = channel.getPermissionContainer();
        final Guild guild = channel.getGuild();
        for (final PermissionOverride permissionOverride : container.getPermissionOverrides()) {
            final long id = permissionOverride.getIdLong();
            if (id != corner.owner && permissionOverride.getAllowed().contains(Permission.VIEW_CHANNEL)) existing.add(id);
        }

        // Get added and removed users/roles
        final List<Long> selected = new ArrayList<>(event.getValues().stream()
                .map(IMentionable::getIdLong)
                .toList());
        selected.remove(corner.owner);
        final List<Long> added = new ArrayList<>(selected);
        added.removeAll(existing);
        final List<Long> removed = new ArrayList<>(existing);
        removed.removeAll(selected);

        // Update permissions
        for (final long id : added) {
            // Role
            final Role role = guild.getRoleById(id);
            if (role != null) {
                container.upsertPermissionOverride(role).setAllowed(PERMISSIONS).queue();
                continue;
            }

            // User
            guild.retrieveMemberById(id)
                    .flatMap(member -> container.upsertPermissionOverride(member).setAllowed(PERMISSIONS))
                    .queue();
        }
        final List<Member> inChannel = channel.asAudioChannel().getMembers();
        for (final long id : removed) {
            // Role
            final Role role = guild.getRoleById(id);
            if (role != null) {
                final PermissionOverride override = container.getPermissionOverride(role);
                if (override != null) override.delete().queue();
                continue;
            }

            // User
            guild.retrieveMemberById(id)
                    .queue(member -> {
                        // Update permissions
                        final PermissionOverride override = container.getPermissionOverride(member);
                        if (override != null) override.delete().queue();
                        // Kick if in channel
                        if (inChannel.contains(member)) guild.kickVoiceMember(member).queue();
                    });
        }

        // Reply
        event.getHook().editOriginal(LazyEmoji.YES + " Updated users for **" + channel.getName() + "**!")
                .setComponents(corner.getComponents(buttons, menus, channel, Corner.Data.usersRoles(selected)))
                .queue();
    }

    @Nullable
    private Corner getCorner(@NotNull IReplyCallback event) {
        final long channelId = event.getChannelIdLong();
        final Corner corner = mongo.database.getMagicCollection(Corner.class)
                .findOne(Filters.eq("_id", channelId))
                .orElse(null);
        if (corner == null) event.reply(LazyEmoji.NO + " <#" + channelId + "> is not a Corner!").setEphemeral(true).queue();
        return corner;
    }
}
