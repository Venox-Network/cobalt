const { Client, ContextMenuInteraction, MessageEmbed } = require("discord.js");

module.exports = {
  name: "User Info",
  type: "USER",
  permissions:"KICK_MEMBERS " || "BAN_MEMBERS",
  /**
   *
   * @param {Client} client
   * @param {ContextMenuInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    const target = await interaction.guild.members.fetch(interaction.targetId)

    const embed = new MessageEmbed()
            .setColor('0070c0')
            .setAuthor(target.user.tag)
            .setDescription(`${target.user}`)
            .setThumbnail(target.user.AvatarURL)
            .addField('Member Since:', `<t:${parseInt(target.joinedTimestamp / 1000)}:R>`, true)
            .addField("Discord User Since:", `<t:${parseInt(target.user.createdTimestamp / 1000)}:R>`, true)
            .addField("Nickname:", `${target.user.nickname !== null ? `${member.nickname}` : 'None'}`, true)
            .addField("Roles:", target.roles.cache.map(roles => roles).replace('@everyone', ' ') || "No roles", true)
            .addField("Custom Status:", `${target.user.presence.game ? target.user.presence.game.name : 'None'}`, true)
            .addField("Status:", `${target.user.presence.status}`, true)
            .addField('User\'s ID:', target.user.id, true)
            .setFooter(`Venox Network | Guild ID: ${interaction.guild.id}`, `https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png`);

    interaction.reply({ embeds: [embed], ephemeral: true});
  },
};
