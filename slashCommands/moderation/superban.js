const { CommandInteraction, Client } = require("discord.js");
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
    {
      name: "reason",
      description: "reason for the superban",
      type: "STRING",
      required: false,
    },
  ],
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
   run: async (client, interaction) => {
    if (!owners.includes(interaction.user.id))
        return interaction.followUp(
          "Only the bot owner can use this command."
        );
        const targetID = interaction.options.getUser("user").id;
        const target = interaction.options.getUser("user"); 
    if (!targetID) return interaction.followUp("Please provide the targets ID");

    //client.guilds.cache.forEach(a => a.members.ban(targetID));

    target.send(`You've been banned from all Venox Network servers\n> Reason: ${reason || `no reason provided`}`);

    interaction.followUp(`**Successfully banned ** <@${targetID}>`);
    
  },
};
