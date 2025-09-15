package network.venox.cobalt.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Corner;
import network.venox.cobalt.mongo.CornerCreator;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyListener;
import xyz.srnyx.lazylibrary.events.GuildVoiceJoinEvent;
import xyz.srnyx.lazylibrary.events.GuildVoiceLeaveEvent;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.List;
import java.util.Optional;


public class GuildVoiceListener extends LazyListener {
    @NotNull private static final ErrorHandler IGNORE_UNKNOWN_CHANNEL = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL);

    @NotNull private final MongoProvider mongo;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus menus;

    public GuildVoiceListener(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus menus) {
        this.mongo = mongo;
        this.buttons = buttons;
        this.menus = menus;
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        final long channelId = event.getChannelJoined().getIdLong();

        // CornerCreator
        final CornerCreator cornerCreator = mongo.database.getMagicCollection(CornerCreator.class)
                .findOne(Filters.eq("_id", channelId))
                .orElse(null);
        if (cornerCreator != null) {
            cornerCreator.createCorner(mongo, buttons, menus, event.getMember(), event.getChannelJoined());
            return;
        }

        // Mute role
        muteRole(event);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        final AudioChannelUnion channel = event.getChannelLeft();
        final Bson idFilter = Filters.eq("_id", channel.getIdLong());
        final MagicCollection<Corner> collection = mongo.database.getMagicCollection(Corner.class);

        // Get Corner
        final Corner corner = collection
                .findOne(idFilter)
                .orElse(null);
        if (corner == null) return;

        // Last person in VC, delete corner
        final List<Member> members = channel.getMembers();
        if (members.isEmpty()) {
            collection.deleteOne(idFilter);
            channel.delete().queue(null, IGNORE_UNKNOWN_CHANNEL);
            return;
        }

        // Transfer ownership if owner left
        final Member owner = event.getMember();
        if (corner.owner != owner.getIdLong()) return;
        channel.getPermissionContainer().getManager().removePermissionOverride(owner).queue();
        final Member newOwner = members.getFirst();
        corner.owner = newOwner.getIdLong();
        collection.updateOne(idFilter, Updates.set(Corner.PROP_OWNER, corner.owner));
        corner.ownerPermissions(channel, newOwner).queue();
    }

    private void muteRole(@NotNull GuildVoiceJoinEvent event) {
        final Guild guild = event.getGuild();
        final MagicCollection<Server> collection = mongo.database.getMagicCollection(Server.class);
        final Bson filter = Filters.eq("_id", guild.getIdLong());
        final Server server = collection.findOne(filter).orElse(null);
        if (server == null) return;
        final Optional<Role> muteRole = server.muteRole(event.getJDA());
        if (muteRole.isEmpty()) return;
        final Member member = event.getMember();
        final GuildVoiceState voiceState = member.getVoiceState();
        final boolean isVoiceMuted = voiceState != null && voiceState.isGuildMuted();

        // Has mute role (voice mute)
        if (member.getRoles().contains(muteRole.get())) {
            if (!isVoiceMuted) {
                guild.mute(member, true).queue();
                collection.updateOne(filter, Updates.addToSet(Server.PROP_MUTED_USERS, member.getIdLong()));
            }
            return;
        }

        // Doesn't have mute role (voice unmute)
        if (isVoiceMuted && server.mutedUsers().contains(member.getIdLong())) {
            guild.mute(member, false).queue();
            collection.updateOne(filter, Updates.pull(Server.PROP_MUTED_USERS, member.getIdLong()));
        }
    }
}
