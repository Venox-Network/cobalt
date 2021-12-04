const {
  Message,
  Client,
  MessageEmbed,
  MessageActionRow,
  MessageSelectMenu,
} = require("discord.js");

module.exports = {
  name: "test",
  aliases: ["t"],
  description: "test",
  /**
   *
   * @param {Client} client
   * @param {Message} message
   * @param {String[]} args
   */
  run: async (client, message, args) => {
    const embed = new MessageEmbed()
      .setColor("2f3136")
      .setTitle("FAQ")
      .setDescription("Frequently Asked Questions");

    const components = new MessageActionRow().addComponents(
      new MessageSelectMenu()
        .setCustomId("faq-menu")
        .setPlaceholder("Choose a question")
        .addOptions([
          {
            label: "FAQ #1",
            value:
              "hibro",
            description: "What is this place?!",
          },
          {
            label: "FAQ #2",
            value:
              [{embed: embed}],
            
            description: "What are the channels?",
          },
        ])
    );

    message.channel.send({
      embeds: [embed],
      components: [components],
    });

    const filter = (interaction) =>
      interaction.isSelectMenu() && interaction.user.id === message.author.id;

    const collector = message.channel.createMessageComponentCollector({
      filter,
      // time: 5000,
    });

    collector.on("collect", async (collected) => {
      const value = collected.values[0];
      collected.deferUpdate()
      collected.followUp({ content: value, ephemeral: true });
    });
  },
};
