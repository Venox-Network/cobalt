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
              embed,
            description: "What is this place?!",
          },
          {
            label: "FAQ #2",
            value:
              ">>> <#888249592762740777> - List of servers <@242385234992037888> and <@273538684526264320> administrate/moderate.\n<#888249592762740777> - Information about <@731650802296422470>.\n<#888249592762740777> - Information about <@633461362734923811>.\n<#888249592762740777> - Every time someone clicks on https://dsc.gg/venoxnet, a message is sent here.",
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
