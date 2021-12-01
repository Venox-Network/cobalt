const {
  Client,
  MessageEmbed,
  MessageActionRow,
  MessageSelectMenu,
  CommandInteraction,
} = require("discord.js");

module.exports = {
  name: "test",
  description: "this is a test",
  type: "CHAT_INPUT",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
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

    interaction
      .followUp({
        embeds: [embed],
        components: [components],
        ephemeral: true,
      })
      .catch(console.error);

    const filter = (interaction) => interaction.isSelectMenu() && interaction.user.id === interaction.author.id;

    const collector = interaction.channel.createMessageComponentCollector({
        filter
        // time: 5000,
      });

      collector.on("collect", async (collected) => {
          const value = collected.values[0];
          collected.deferUpdate()
          collected.followUp({ content: value, ephemeral: true });
      })

  },
};
