const {  Message,  Client,  MessageEmbed,  MessageActionRow,  MessageSelectMenu,} = require("discord.js");

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
      .setTitle("Venox Commands")
      .setDescription("Choose a category");

    const components = (state) => [
      new MessageActionRow().addComponents(
        new MessageSelectMenu()
          .setCustomId("help-menu")
          .setPlaceholder("Select a category")
          .setDisabled(state)
          .addOptions("1", "2", "3"
          )
      ),
    ];

    const initialMessage = await message.channel.send({
      embeds: [embed],
      components: components(false),
    });

    const filter = (interaction) => interaction.user.id === message.author.id;

    const collector = message.channel.createMessageComponentCollector({
      filter,
      componentType: "SELECT_MENU",
      // time: 5000,
    });

    collector.on("collect", (interaction) => {
      

      const categoryEmbed = new MessageEmbed()
        .setTitle(`idk`)
        .setColor("0070c0")
        // .setDescription('Command List')
        .addFields("a", "a", "c"
        );

      interaction.update({ embeds: [categoryEmbed] });

      // interaction.reply({embeds: [categoryEmbed], ephermal: true})
    });

    collector.on("end", () => {
      initialMessage.edit({ components: components(true) });
    });
  },
};
