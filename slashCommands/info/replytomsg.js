const { Client, ContextMenuInteraction } = require("discord.js");

module.exports = {
  name: "replytomsg",
  type: "MESSAGE",
  /**
   *
   * @param {Client} client
   * @param {ContextMenuInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    interaction.followUp({ content: "ello :)" });
  },
};
