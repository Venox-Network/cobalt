const {Client, ContextMenuInteraction, MessageEmbed} = require("discord.js");

module.exports = {
  name: "User Info",
  type: "USER",
  /**
   *
   * @param {Client} client
   * @param {ContextMenuInteraction} interaction
   */
  run: async (client, interaction) => {
    const target = await interaction.guild.members.fetch(interaction.targetId);
    const embed = new MessageEmbed()
      .setColor(target.roles.cache.size - 1 ? target.displayHexColor : "b9bbbe" )
        //FIXME Deprecated symbol used
      .setAuthor(target.user.tag)
      .setDescription(`${target.user}`)
      .setThumbnail(target.user.avatarURL({ dynamic: true, size: 512 }))
      .addField(
        "Member Since:",
          //FIXME Argument type number is not assignable to parameter type string
        `<t:${parseInt(target.joinedTimestamp / 1000)}:R>`,
        true
      )
      .addField(
        "Discord User Since:",
          //FIXME Argument type number is not assignable to parameter type string
        `<t:${parseInt(target.user.createdTimestamp / 1000)}:R>`,
        true
      )
      .addField(
        "Roles:", target.roles.cache.size - 1 ?
        target.roles.cache
          .map((r) => r)
          .join(" ").replace("@everyone", " ") : "No roles"
          /**/
      )
      /*.addField("Custom Status:", `${target.user.presence.game ? target.user.presence.game.name : 'Null'}`, true)
      .addField("Status:", `${target.user.presence.status ? target.user.presence.status.name : 'Null'}`, true)
      .addField(
        "Mutual Servers",
        await client.guilds.cache
          .filter((u) => u.members.cache.get(interaction.user.id))
          .map((g) => g.name)
          .join(", "),
        true
      )*/
      .addField("User's ID:", `${target.user.id}`, true)
        //FIXME Deprecated symbol used
      .setFooter(
        "Venox Network",
        "https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png"
      );

    await interaction.followUp({embeds: [embed], ephemeral: true});
  },
};
