const { Client, CommandInteraction } = require("discord.js");

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
    if (interaction.guild.me.voice.channelId) {
        interaction.guild.me.voice.channel.leave();
      interaction.followUp({content: "Disconnected ✅"});
    } else {
      interaction.followUp({content: "I'm not connected to a voice channel.."});
    }
  },
};
