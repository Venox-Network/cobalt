const { Message, Client } = require("discord.js");
const owners = ["273538684526264320", "242385234992037888"];

module.exports = {
  name: "superban",
  description: "bans a user in every the server the bot is in (owner only)",
  options: [
    {
      name: "user",
      description: "member to superban",
      type: "USER",
      required: true,
    },
  ],
  /**
   *
   * @param {Client} client
   * @param {Message} message
   * @param {String[]} args
   */
   run: async (client, interaction) => {
    if (!owners.includes(interaction.user.id))
        return interaction.followUp(
          "Only the bot owner can use this command."
        );
        const targetID = interaction.options.getUser("user").id; 
    if (!targetID) return interaction.followUp("Please provide the targets ID");

    client.guilds.cache.forEach(a => a.members.ban(targetID));

    interaction.followUp(`**Successfully banned ** <@${targetID}>`);
    
  },
};
