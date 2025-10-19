package network.venox.cobalt.mongo;

import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.EntitySelectMenu;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

import network.venox.cobalt.components.CornerComponents;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.*;


public class Corner {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_OWNER = "owner";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_OWNER) public long owner;

    public Corner() {}

    public Corner(@NotNull AudioChannelUnion channel, long owner) {
        this.channel = channel.getIdLong();
        this.guild = channel.getGuild().getIdLong();
        this.owner = owner;
    }

    @NotNull
    public AuditableRestAction<PermissionOverride> ownerPermissions(@NotNull AudioChannelUnion audioChannel, @NotNull Member member) {
        final PermissionOverrideAction action = audioChannel.upsertPermissionOverride(member);
        final EnumSet<Permission> allowed = action.getAllowedPermissions();
        allowed.add(Permission.VIEW_CHANNEL);
        allowed.add(Permission.MANAGE_CHANNEL);
        allowed.add(Permission.MESSAGE_SEND);
        allowed.add(Permission.MESSAGE_HISTORY);
        allowed.add(Permission.VOICE_CONNECT);
        allowed.add(Permission.VOICE_SPEAK);
        allowed.add(Permission.VOICE_USE_VAD);
        allowed.add(Permission.VOICE_MOVE_OTHERS);
        return action
                .setAllowed(allowed)
                .reason("Corner owner permissions");
    }

    @NotNull
    public List<ActionRow> getComponents(@NotNull Buttons buttons, @NotNull SelectMenus menus, @NotNull GuildMessageChannel channel, @Nullable Corner.Data data) {
        if (data == null) data = new Data();
        final List<ActionRow> rows = new ArrayList<>();
        final InteractionConstraints constraints = InteractionConstraints.ofUserIds(owner).addPermissions(Permission.MANAGE_PERMISSIONS);

        // Toggle lock button
        rows.add(ActionRow.of(
                buttons.of(data.isLocked(channel)
                                ? LazyEmoji.UNLOCK_CLEAR_DARK.getButtonContent(ButtonStyle.SUCCESS, "Unlock")
                                : LazyEmoji.LOCK_CLEAR_DARK.getButtonContent(ButtonStyle.DANGER, "Lock")).persistent()
                        .bindTo(CornerComponents.LOCK_BUTTON)
                        .constraints(constraints)
                        .build()));

        // Get current blacklist
        final List<EntitySelectMenu.DefaultValue> blacklistValues = new ArrayList<>();
        for (final Long id : data.getBlacklist(channel)) blacklistValues.add(EntitySelectMenu.DefaultValue.user(id));

        // Add blacklist menu
        rows.add(ActionRow.of(buttons.secondary("───── BLACKLIST ─────").ephemeral().build().asDisabled()));
        rows.add(ActionRow.of(menus.entitySelectMenu(EntitySelectMenu.SelectTarget.USER).persistent()
                .bindTo(CornerComponents.BLACKLIST_MENU)
                .constraints(constraints)
                .setPlaceholder("Add users to the blacklist")
                .setMinValues(0)
                .setMaxValues(EntitySelectMenu.OPTIONS_MAX_AMOUNT)
                .setDefaultValues(blacklistValues)
                .build()));

        // Get users/roles
        final List<EntitySelectMenu.DefaultValue> userValues = new ArrayList<>();
        if (data.isLocked(channel)) for (final Map.Entry<Long, EntitySelectMenu.SelectTarget> entry : data.getUsersRoles(channel).entrySet()) {
            final long id = entry.getKey();
            // User
            if (entry.getValue() == EntitySelectMenu.SelectTarget.USER) {
                userValues.add(EntitySelectMenu.DefaultValue.user(id));
                continue;
            }
            // Role
            userValues.add(EntitySelectMenu.DefaultValue.role(id));
        }

        // Add users/roles menu
        final EntitySelectMenu.Builder usersRolesMenu = menus.entitySelectMenu(EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE).persistent()
                .bindTo(CornerComponents.USERS_ROLES_MENU)
                .constraints(constraints)
                .setPlaceholder(data.isLocked(channel) ? "Add users/roles to " + channel.getName() : "Lock to manage users/roles!")
                .setMinValues(0)
                .setMaxValues(EntitySelectMenu.OPTIONS_MAX_AMOUNT);
        if (data.isLocked(channel)) {
            usersRolesMenu.setDefaultValues(userValues);
        } else {
            usersRolesMenu.setDisabled(true);
        }
        rows.add(ActionRow.of(buttons.secondary("───── USERS / ROLES ─────").ephemeral().build().asDisabled()));
        rows.add(ActionRow.of(usersRolesMenu.build()));

        return rows;
    }

    public static boolean isLocked(@NotNull GuildMessageChannel channel) {
        final PermissionOverride override = channel.getPermissionContainer().getPermissionOverride(channel.getGuild().getPublicRole());
        return override != null && override.getDenied().contains(Permission.VIEW_CHANNEL);
    }

    public static class Data {
        @Nullable private final List<Long> blacklist;
        @Nullable private final Boolean locked;
        @Nullable private final List<Long> usersRoles;
        @Nullable private Map<Long, EntitySelectMenu.SelectTarget> usersRolesMap;

        public Data() {
            this.blacklist = null;
            this.locked = null;
            this.usersRoles = null;
        }

        private Data(@Nullable List<Long> blacklist, @Nullable Boolean locked, @Nullable List<Long> usersRoles) {
            this.blacklist = blacklist;
            this.locked = locked;
            this.usersRoles = usersRoles;
        }

        @NotNull
        public static Data blacklist(@NotNull List<Long> blacklist) {
            return new Data(blacklist, true, null);
        }

        @NotNull
        public static Data locked(boolean locked) {
            return new Data(null, locked, null);
        }

        @NotNull
        public static Data usersRoles(@NotNull List<Long> usersRoles) {
            return new Data(null, true, usersRoles);
        }

        @NotNull
        public List<Long> getBlacklist(@NotNull GuildMessageChannel channel) {
            if (blacklist != null) return blacklist;
            final List<Long> ids = new ArrayList<>();
            final IPermissionContainer container = channel.getPermissionContainer();
            for (final PermissionOverride override : container.getMemberPermissionOverrides()) {
                if (override.getDenied().contains(Permission.VIEW_CHANNEL)) ids.add(override.getIdLong());
            }
            return ids;
        }

        public boolean isLocked(@NotNull GuildMessageChannel channel) {
            return locked != null ? locked : Corner.isLocked(channel);
        }

        @NotNull
        public Map<Long, EntitySelectMenu.SelectTarget> getUsersRoles(@NotNull GuildMessageChannel channel) {
            if (usersRolesMap != null) return usersRolesMap;
            usersRolesMap = new HashMap<>();

            if (usersRoles != null) {
                final Guild guild = channel.getGuild();
                for (final Long id : usersRoles) usersRolesMap.put(id, guild.getRoleById(id) != null ? EntitySelectMenu.SelectTarget.ROLE : EntitySelectMenu.SelectTarget.USER);
                return usersRolesMap;
            }

            // Get users/roles
            for (final PermissionOverride override : channel.getPermissionContainer().getPermissionOverrides()) {
                if (!override.getAllowed().contains(Permission.VIEW_CHANNEL)) continue;
                final long id = override.getIdLong();
                if (id != channel.getGuild().getOwnerIdLong()) usersRolesMap.put(id, override.isRoleOverride() ? EntitySelectMenu.SelectTarget.ROLE : EntitySelectMenu.SelectTarget.USER);
            }
            return usersRolesMap;
        }
    }
}
