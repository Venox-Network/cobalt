const { Client, ContextMenuInteraction } = require("discord.js");

module.exports = {
  name: "replytomsg",
  type: "CHAT_INPUT",
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
