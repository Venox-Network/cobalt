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
        allowed.add(Permission.MANAGE_CHANNEL); //TODO need to know if this is dangerous
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
    public List<ActionRow> getComponents(@NotNull Buttons buttons, @NotNull SelectMenus menus, @NotNull GuildMessageChannel channel,
                                         @Nullable List<Long> blacklist, @Nullable Boolean locked, @Nullable List<Long> usersRoles) {
        if (locked == null) locked = isLocked(channel);
        final List<ActionRow> rows = new ArrayList<>();
        final InteractionConstraints constraints = InteractionConstraints.ofUserIds(owner).addPermissions(Permission.MANAGE_PERMISSIONS);

        // Toggle lock button
        rows.add(ActionRow.of(
                buttons.of(locked
                                ? LazyEmoji.UNLOCK_CLEAR_DARK.getButtonContent(ButtonStyle.SUCCESS, "Unlock")
                                : LazyEmoji.LOCK_CLEAR_DARK.getButtonContent(ButtonStyle.DANGER, "Lock")).persistent()
                        .bindTo(CornerComponents.LOCK_BUTTON)
                        .constraints(constraints)
                        .build()));

        final IPermissionContainer container = channel.getPermissionContainer();

        // Get current blacklist
        final List<EntitySelectMenu.DefaultValue> blacklistValues = new ArrayList<>();
        if (blacklist != null) {
            for (final Long id : blacklist) blacklistValues.add(EntitySelectMenu.DefaultValue.user(id));
        } else {
            for (final PermissionOverride override : container.getMemberPermissionOverrides()) {
                if (override.getDenied().contains(Permission.VIEW_CHANNEL)) blacklistValues.add(EntitySelectMenu.DefaultValue.user(override.getIdLong()));
            }
        }

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
        if (locked) {
            final Map<Long, EntitySelectMenu.SelectTarget> existing = new HashMap<>();
            if (usersRoles != null) {
                final Guild jdaGuild = channel.getGuild();
                for (final Long id : usersRoles) {
                    existing.put(id, jdaGuild.getRoleById(id) != null ? EntitySelectMenu.SelectTarget.ROLE : EntitySelectMenu.SelectTarget.USER);
                }
            } else {
                for (final PermissionOverride override : container.getPermissionOverrides()) {
                    if (!override.getAllowed().contains(Permission.VIEW_CHANNEL)) continue;
                    final long id = override.getIdLong();
                    if (id != owner) existing.put(id, override.isRoleOverride() ? EntitySelectMenu.SelectTarget.ROLE : EntitySelectMenu.SelectTarget.USER);
                }
            }
            for (final Map.Entry<Long, EntitySelectMenu.SelectTarget> entry : existing.entrySet()) {
                final long id = entry.getKey();
                if (entry.getValue() == EntitySelectMenu.SelectTarget.USER) {
                    userValues.add(EntitySelectMenu.DefaultValue.user(id));
                } else {
                    userValues.add(EntitySelectMenu.DefaultValue.role(id));
                }
            }
        }

        // Add users/roles menu
        final EntitySelectMenu.Builder usersRolesMenu = menus.entitySelectMenu(EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE).persistent()
                .bindTo(CornerComponents.USERS_ROLES_MENU)
                .constraints(constraints)
                .setPlaceholder(locked ? "Add users/roles to " + channel.getName() : "Lock to manage users/roles!")
                .setMinValues(0)
                .setMaxValues(EntitySelectMenu.OPTIONS_MAX_AMOUNT);
        if (locked) {
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
}
