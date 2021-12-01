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
    //console.log(categories);
    const embed = new MessageEmbed()
      .setColor("0070c0")
      .setTitle("ello :)")
      .setDescription("Choose a category");

    const components = new MessageActionRow().addComponents(
      new MessageSelectMenu()
        .setCustomId("help-menu")
        .setPlaceholder("Select a category")
        .addOptions([
          {
            label: "first option",
            value: "first",
            description: "first boi",
          },
          {
            label: "second option",
            value: "second",
            description: "second boi",
          },
          {
            label: "third option",
            value: "third",
            description: "third boi",
          },
          {
            label: "fourth option",
            value: "fourth",
            description: "fourth boi",
          },
        ])
    );

    message.channel.send({
      embeds: [embed],
      components: [components],
    });

    const filter = (interaction) => interaction.isSelectMenu() && interaction.user.id === message.author.id;

    const collector = message.channel.createMessageComponentCollector({
        filter
        // time: 5000,
      });

      collector.on("collect", async(collected) => {
          const value = collected.values[0];
          collected.deferUpdate()
          collected.reply({ content: value});
      })


  },
};
