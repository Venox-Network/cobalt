package network.venox.cobalt.mongo;

import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import network.venox.cobalt.MongoProvider;

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

    public void createCorner(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus menus, @NotNull Member member, @NotNull AudioChannelUnion voiceChannel) {
        final Guild guild = voiceChannel.getGuild();
        guild.createCopyOfChannel(voiceChannel)
                .setName(member.getEffectiveName() + "'s Corner")
                .queue(cornerChannel -> {
                    final Corner corner = new Corner(cornerChannel, member.getIdLong());
                    mongo.database.getMagicCollection(Corner.class).insertOne(corner);
                    corner.ownerPermissions(cornerChannel, member).queue();
                    guild.moveVoiceMember(member, cornerChannel).queue();
                    cornerChannel.asGuildMessageChannel()
                            .sendMessage(member.getAsMention())
                            .setComponents(corner.getComponents(buttons, menus, cornerChannel.asGuildMessageChannel(), null, null, null))
                            .queue();
                });
    }
}
