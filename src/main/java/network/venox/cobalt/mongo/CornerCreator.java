package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;


public class CornerCreator {
    @NotNull public static final String PROP_GUILD = "guild";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;

    public CornerCreator() {}

    public CornerCreator(@NotNull VoiceChannel channel) {
        this.channel = channel.getIdLong();
        this.guild = channel.getGuild().getIdLong();
    }

    public void createCorner(@NotNull Cobalt bot, @NotNull Member member, @NotNull AudioChannelUnion voiceChannel) {
        final Guild guild = voiceChannel.getGuild();
        guild.createCopyOfChannel(voiceChannel)
                .setName(member.getEffectiveName() + "'s Corner")
                .flatMap(cornerChannel -> {
                    final Corner corner = new Corner(cornerChannel, member.getIdLong());
                    bot.mongo.getMagicCollection(Corner.class).insertOne(corner);
                    return corner.ownerPermissions(cornerChannel, member)
                            .flatMap(_ -> guild.moveVoiceMember(member, cornerChannel));
                })
                .queue();
    }
}
