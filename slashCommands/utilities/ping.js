const { Client, CommandInteraction, MessageEmbed } = require("discord.js");

module.exports = {
  name: "ping",
  description: "returns websocket ping",
  type: "CHAT_INPUT",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   */
  run: async (client, interaction) => {
    await interaction.followUp("Pinging...");
    const reply = await interaction.fetchReply();
    const embed = new MessageEmbed()
      .setTitle("Pong!")
        //FIXME Signature mismatch
      .setColor("0070c0")
      .addField(
        "❤️ Heartbeat", 
        `${client.ws.ping}ms`
        )
      .addField(
        "🔁 Roundtrip",
          //FIXME Unresolved variable
        `${reply.createdTimestamp - interaction.createdTimestamp}ms`
      )
        //FIXME Deprecated symbol used
      .setFooter(
        "Venox Network",
        "https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png"
      );
    //FIXME Unresolved function or method
    reply.edit({ content: " ", embeds: [embed] });
  },
};
