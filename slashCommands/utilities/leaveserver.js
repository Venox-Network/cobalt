const { Client, CommandInteraction, MessageEmbed } = require("discord.js");
const owners = ["273538684526264320", "242385234992037888"];

module.exports = {
  name: "leaveserver",
  description: "Leaves the server",
  run: async (client, interaction, args) => {
    if (!owners.includes(interaction.user.id))
      return interaction.followUp("Only the bot owner(s) can use this command.");
    message.guild.leave();
  },
};
