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
            description: "numba 1",
          },
          {
            label: "first option",
            value: "first",
            description: "numba 1",
          },
          {
            label: "first option",
            value: "first",
            description: "numba 1",
          },
          {
            label: "first option",
            value: "first",
            description: "numba 1",
          },
        ])
    );

    message.channel.send({
      embeds: [embed],
      components: [components],
    });
  },
};
