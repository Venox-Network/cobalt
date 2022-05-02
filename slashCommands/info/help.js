const {
  CommandInteraction,
  Client,
  MessageEmbed,
  MessageActionRow,
  MessageSelectMenu,
} = require("discord.js");

module.exports = {
  name: "help",
  description: "lists all the commands",
  type: "CHAT_INPUT",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   */
  run: async (client, interaction) => {
    const emojis = {
      info: "ℹ",
      utilities: "🔧",
      moderation: "⚒",
      fun: "🎮",
      music: "🎶",
    };

    const directories = [
        //FIXME Unresolved variable
      ...new Set(client.commands.map((cmd) => cmd.directory)),
    ];

    const formatStr = (str) =>
      `${str[0].toUpperCase()}${str.slice(1).toLowerCase()}`;

    const categories = directories.map((dir) => {
      //FIXME Unresolved variable
      const getCmd = client.commands
        .filter((cmd) => cmd.directory === dir)
        .map((cmd) => {
          return {
            name: cmd.name || "null",
            description: cmd.description || "null",
          };
        });

      return {
        directory: formatStr(dir),
        commands: getCmd,
      };
    });

    const embed = new MessageEmbed()
        //FIXME Signature mismatch
      .setColor("0070c0")
      .setTitle("Venox Commands")
      .setDescription("Choose a category")
        //FIXME Deprecated symbol used
      .setFooter(
        "Venox Network",
        "https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png"
      );

    const components = (state) => [
      new MessageActionRow().addComponents(
          //FIXME Signature mismatch
        new MessageSelectMenu()
          .setCustomId("help-menu")
          .setPlaceholder("Select a category")
          .setDisabled(state)
          .addOptions(
              //FIXME Signature mismatch
            categories.map((cmd) => {
              return {
                label: cmd.directory,
                value: cmd.directory.toLowerCase(),
                description: `${cmd.directory} category`,
                emoji: emojis[cmd.directory.toLowerCase()] || null,
              };
            })
          )
      ),
    ];

    const initialMessage = await interaction.followUp({
      embeds: [embed],
      components: components(false),
    });

    const filter = (interaction) => !interaction.user.bot;

    const collector = interaction.channel.createMessageComponentCollector({
      filter,
      componentType: "SELECT_MENU",
      time: 60000,
    });

    collector.on("collect", (interaction) => {
      const [directory] = interaction.values;

      const category = categories.find(
        (x) => x.directory.toLowerCase() === directory
      );

      const categoryEmbed = new MessageEmbed()
        .setTitle(`${directory.toUpperCase()}`)
          //FIXME Signature mismatch
        .setColor("0070c0")
          //FIXME Deprecated symbol used
        .setFooter(
          "Venox Network",
          "https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png"
        )
        // .setDescription('Command List')
        .addFields(
          category.commands.map((cmd) => {
            return {
              name: `\`${cmd.name}\``,
              value: `${cmd.description}`,
              inline: true,
            };
          })
        );

      interaction.update({ embeds: [categoryEmbed] });

      // interaction.reply({embeds: [categoryEmbed], ephemeral: true})
    });

    collector.on("end", () => {
      //FIXME Unresolved function or method
      initialMessage.edit({ components: components(true) });
    });
  },
};
