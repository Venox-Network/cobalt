const { Client, CommandInteraction } = require("discord.js");

module.exports = {
  name: "leave",
  description: "returns websocket ping",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    if (interaction.guild.me.voiceChannel !== undefined) {
      interaction.guild.me.voiceChannel.leave();
      interaction.reply({content: "Disconnected ✅", ephemeral:true});
    } else {
      interaction.reply("I'm not connected to a voice channel!");
    }
  },
};
