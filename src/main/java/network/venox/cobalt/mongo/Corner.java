package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;


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
//        allowed.add(Permission.MANAGE_PERMISSIONS); //TODO they can give dangerous permissions, make commands to manage roles/users instead
        allowed.add(Permission.MESSAGE_SEND);
        allowed.add(Permission.MESSAGE_HISTORY);
        allowed.add(Permission.VOICE_CONNECT);
        allowed.add(Permission.VOICE_SPEAK);
        allowed.add(Permission.VOICE_USE_VAD);
        allowed.add(Permission.VOICE_MOVE_OTHERS);
        allowed.add(Permission.VOICE_MUTE_OTHERS); //TODO need to test if this mutes across entire guild or just in the channel
        allowed.add(Permission.VOICE_DEAF_OTHERS); //TODO need to test if this deafens across entire guild or just in the channel
        return action
                .setAllowed(allowed)
                .reason("Corner owner permissions");
    }
}
