package network.venox.cobalt.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyListener;
import xyz.srnyx.lazylibrary.events.GuildVoiceJoinEvent;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.Optional;


public final class GuildVoiceListener extends LazyListener {
    private final @NotNull MongoProvider mongo;

    public GuildVoiceListener(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
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
