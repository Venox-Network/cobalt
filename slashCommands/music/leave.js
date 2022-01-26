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
    const connection = getVoiceConnection(channel.guild.id);
    const queue = player.getQueue(interaction.guildId);

    if (channel) {
        await queue?.playing ? queue.destroy() : interaction.followUp({content: "Nothing was in the queue", ephemeral:true});
        await interaction.followUp({content: "Disconnected ✅", ephemeral: true});
    } else {
      interaction.followUp({content: "I'm not connected to a voice channel.."});
    }

    
    

  },
};
