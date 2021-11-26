const { Client, MessageEmbed, CommandInteraction } = require("discord.js");

module.exports = {
  name: "userinfo",
  description: "returns user information",
  type: "CHAT_INPUT",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    const user = await client.users.fetch(interaction.targetId);
    if (!user)
      return interaction.followUp({ content: "Couldn't find that user :(" });
    const member = interaction.guild.member(user);
    const embed = new MessageEmbed()
      .setColor("0070c0")
      .setAuthor(user.tag)
      .setDescription(`${user}`)
      .setThumbnail(user.displayAvatarURL({dynamic: true}))
      .addField("Join Date:", `${member.joinedAt}`, true)
      .addField("Account Creation Date:", member.user.createdAt, true)
      .addField(
        "Nickname:",
        `${member.nickname !== null ? `${member.nickname}` : "None"}`,
        true
      )
      .addField(
        "Roles:",
        member.roles.cache.map((roles) => `${roles}`).join(", "),
        true
      )
      .addField(
        "Custom Status:",
        `${user.presence.game ? user.presence.game.name : "None"}`,
        true
      )
      .addField("Status:", `${user.presence.status}`, true)
      .addField("User's ID:", user.id, true)

      .setFooter(
        `© ${interaction.guild.me.displayName} | Guild ID: ${interaction.guild.id}`
      );

    interaction
      .followUp({
        embeds: [embed],
        ephemeral: true,
      })
      .catch(console.error);
  },
};
