const { Client, CommandInteraction } = require("discord.js");
const { getVoiceConnection } = require('@discordjs/voice');
const { joinVoiceChannel } = require('@discordjs/voice');
const player = require("../../client/player");

module.exports = {
  name: "leave",
  description: "leaves the voice channel",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    const channel = interaction.member.voice.channel;
    const connection = joinVoiceChannel({
      channelId: channel.id,
      guildId: channel.guild.id,
      adapterCreator: channel.guild.voiceAdapterCreator,
    });

    //const connection = getVoiceConnection(interaction.member.voice.channel);

    if (interaction.member.voice.channel) {
        await queue.delete();
        await connection.destroy();
        await interaction.followUp({content: "Disconnected ✅"});
    } else {
      interaction.followUp({content: "I'm not connected to a voice channel.."});
    }

    const queue = player.getQueue(interaction.guildId);
    
    

  },
};
