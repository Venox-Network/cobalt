package network.venox.cobalt.commands.guild.locking;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.EntitySelectMenu;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.dv8tion.jda.api.requests.RestAction;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Lock;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;
import xyz.srnyx.magicmongo.builders.UpdateBuilder;

import java.util.*;
import java.util.stream.Collectors;


@Command
public class LockingLock {
    @NotNull private final MongoProvider mongo;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus selectMenus;

    public LockingLock(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus selectMenus) {
        this.mongo = mongo;
        this.buttons = buttons;
        this.selectMenus = selectMenus;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS})
    @JDASlashCommand(
            name = "locking",
            subcommand = "lock",
            description = "ADMIN | Lock the channel to specific roles")
    public void lock(@NotNull GuildSlashEvent event,
                     @SlashOption(description = "Optional preset of roles", autocomplete = LockingCommon.AC_PRESET) @Nullable String preset,
                     @SlashOption(description = "Custom content for the sticky message") @Nullable String content,
                     @SlashOption(description = "The channel to lock") @Nullable TextChannel channel) {
        channel = LockingCommon.initialize(event, channel);
        if (channel == null) return;
        final MagicCollection<Lock> lockCollection = mongo.database.getMagicCollection(Lock.class);
        final Bson lockFilter = Filters.eq("_id", channel.getIdLong());

        // Get roles
        final Set<Long> roles = mongo.database.getMagicCollection(Server.class).findOne("_id", event.getGuild().getIdLong())
                .map(server -> server.lockPresets)
                .map(lockPresets -> lockPresets.get(preset))
                .map(presetRoles -> (Set<Long>) new HashSet<>(presetRoles))
                .orElseGet(() -> lockCollection.findOne(lockFilter)
                        .map(value -> value.allowedRoles)
                        .orElseGet(HashSet::new));

        // Reply
        final InteractionConstraints constraints = InteractionConstraints.ofUserIds(event.getUser().getIdLong());
        event.reply(LockingCommon.getMessage(roles, content))
                .setAllowedMentions(Collections.emptySet())
                .setComponents(
                        ActionRow.of(selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.ROLE).ephemeral().bindTo(menu -> {
                            roles.clear();
                            roles.addAll(menu.getValues().stream()
                                    .map(ISnowflake::getIdLong)
                                    .collect(Collectors.toSet()));
                            menu.editMessage(LockingCommon.getMessage(roles, content)).queue();
                        })
                                .constraints(constraints)
                                .setPlaceholder("Add roles")
                                .setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT)
                                .setDefaultValues(roles.stream()
                                        .map(EntitySelectMenu.DefaultValue::role)
                                        .collect(Collectors.toSet()))
                                .build()),
                        ActionRow.of(
                                buttons.success("Done, lock channel", LazyEmoji.YES_CLEAR).ephemeral().bindTo(done -> {
                                    final List<RestAction<?>> actions = new ArrayList<>();

                                    // Get existing previous permissions from existing lock
                                    // We want to use the most original previous permissions
                                    final Map<String, Lock.PreviousPermissions> existing = lockCollection.findOne(lockFilter).map(value -> value.previousPermissions).orElseGet(HashMap::new);
                                    final boolean noExisting = existing.isEmpty();

                                    final TextChannel textChannel = done.getChannel().asTextChannel();
                                    final Guild guild = textChannel.getGuild();
                                    final IPermissionContainer container = textChannel.getPermissionContainer();
                                    final IPermissionContainerManager<?, ?> manager = container.getManager();

                                    // Grant role permissions
                                    for (final long roleId : roles) {
                                        // Get role
                                        final Role role = guild.getRoleById(roleId);
                                        if (role == null) continue;
                                        // Check if role already has permissions
                                        final PermissionOverride override = container.getPermissionOverride(role);
                                        if (override != null) {
                                            // Get previous permissions
                                            if (noExisting) existing.put(String.valueOf(roleId), new Lock.PreviousPermissions(override));
                                            // Grant permissions
                                            final Set<Permission> toGrant = new HashSet<>();
                                            final Set<Permission> denied = override.getDenied();
                                            for (final Permission permission : LockingCommon.PERMISSIONS) if (!denied.contains(permission)) toGrant.add(permission);
                                            actions.add(override.getManager().grant(toGrant));
                                            continue;
                                        }
                                        // Grant permissions
                                        actions.add(manager.putRolePermissionOverride(roleId, LockingCommon.PERMISSIONS, null));
                                    }

                                    final Role everyone = guild.getPublicRole();

                                    // Clear filtered permissions for roles that have them granted
                                    for (final PermissionOverride override : container.getRolePermissionOverrides()) {
                                        // Check if role is allowed or everyone
                                        final long roleId = override.getIdLong();
                                        if (roles.contains(roleId) || roleId == everyone.getIdLong()) continue;
                                        // Get permissions to clear
                                        final Set<Permission> toClear = new HashSet<>();
                                        for (final Permission permission : override.getAllowed()) if (LockingCommon.PERMISSIONS.contains(permission)) toClear.add(permission);
                                        // Clear permissions
                                        if (toClear.isEmpty()) continue;
                                        if (noExisting) existing.put(override.getId(), new Lock.PreviousPermissions(override));
                                        actions.add(override.getManager().clear(toClear));
                                    }

                                    final PermissionOverride everyoneOverride = container.getPermissionOverride(everyone);
                                    // Get previous permissions
                                    if (noExisting && everyoneOverride != null) existing.put(everyone.getId(), new Lock.PreviousPermissions(everyoneOverride));
                                    // Deny everyone permissions
                                    actions.add(container.upsertPermissionOverride(everyone).deny(LockingCommon.PERMISSIONS));

                                    // Save to database
                                    final UpdateBuilder builder = new UpdateBuilder(
                                            Updates.set(Lock.PROP_ALLOWED_ROLES, roles),
                                            Updates.set(Lock.PROP_STICKY_MESSAGE_CONTENT, content));
                                    if (noExisting) builder.add(Updates.set(Lock.PROP_PREVIOUS_PERMISSIONS, existing));
                                    final Lock lock = lockCollection.findOneAndUpsert(lockFilter, builder.build());

                                    // Edit message, update permissions, and send sticky message
                                    done.editMessage(LazyEmoji.YES + " Locked channel to " + LockingCommon.getRolesString(roles))
                                            .setComponents()
                                            .flatMap(_ -> RestAction.allOf(actions))
                                            .flatMap(_ -> lock.replaceStickyMessage(mongo, textChannel))
                                            .queue();
                                })
                                        .constraints(constraints)
                                        .build(),
                                buttons.danger("Cancel, don't lock channel", LazyEmoji.NO_CLEAR_DARK)
                                        .ephemeral()
                                        .constraints(constraints)
                                        .bindTo(cancel -> cancel.editMessage(LazyEmoji.YES + " Cancelled channel locking!").setComponents().queue())
                                        .build()))
                .queue();
    }
}
