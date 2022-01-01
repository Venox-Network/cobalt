const { Message, Client } = require("discord.js");
const owners = ["273538684526264320", "242385234992037888"];

module.exports = {
  name: "superban",
  description: "bans a user in every the server the bot is in",
  aliases: ["sb"],
  /**
   *
   * @param {Client} client
   * @param {Message} message
   * @param {String[]} args
   */
  run: async (client, message, args) => {
    if (!owners.includes(message.author.id))
        return message.channel.send(
          "<:redtick:732759534891958322> Only the bot owner can use this command."
        );
    const targetID = args[1]; // this is the targets UserID
    if (!targetID) return message.channel.send("Please provide the targets ID");

    client.guilds.cache.forEach(a => a.members.ban(targetID));

    message.channel.send(`**Successfully banned ** <${targetID}@>`);
    
  },
};
