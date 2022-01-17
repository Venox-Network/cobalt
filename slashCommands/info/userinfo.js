const { Client, ContextMenuInteraction, MessageEmbed } = require("discord.js");

module.exports = {
  name: "User Info",
  type: "USER",
  userPermissions: "KICK_MEMBERS" || "BAN_MEMBERS",
  /**
   *
   * @param {Client} client
   * @param {ContextMenuInteraction} interaction
   * @param {String[]} args
   */
  run: async (client, interaction, args) => {
    const target = await interaction.guild.members.fetch(interaction.targetId);

    //code to fetch mutual servers
    const guilds = [];
    for (const [, guild] of interaction.client.guilds.cache) {
      await guild.members
        .fetch(interaction.user)
        .then(() => guilds.push(guild))
        .catch((error) => console.log(error));
    }

    //code to generate array of server names & IDs for .addOption() in select menu component
    const servers = [];
    for (let i = 0; i < Object.keys(guilds).length; i++) {
      servers.push({
        label: Object.entries(guilds)[i][1].name,
        value: Object.entries(guilds)[i][1].id,
      });
    }

    const embed = new MessageEmbed()
      .setColor("0070c0")
      .setAuthor(target.user.tag)
      .setDescription(`${target.user}`)
      .setThumbnail(target.user.avatarURL({ dynamic: true, size: 512 }))
      .addField(
        "Member Since:",
        `<t:${parseInt(target.joinedTimestamp / 1000)}:R>`,
        true
      )
      .addField(
        "Discord User Since:",
        `<t:${parseInt(target.user.createdTimestamp / 1000)}:R>`,
        true
      )
      .addField(
        "Roles:",
        target.roles.cache
          .map((r) => r)
          .join(" ")
          .replace("@everyone", " ") || "Null",
        true
      )
      //.addField("Custom Status:", `${target.user.presence.game ? target.user.presence.game.name : 'Null'}`, true)
      //.addField("Status:", `${target.user.presence.status ? target.user.presence.status.name : 'Null'}`, true)
      /*.addField(
        "Mutual Servers",
        client.guilds.cache
          .filter((u) => u.members.cache.get(interaction.user.id))
          .map((g) => g.name)
          .join(", "),
        true
      )*/
      .addField(servers)
      .addField("User's ID:", `${target.user.id}`, true)
      .setFooter(
        `Venox Network`,
        `https://us-east-1.tixte.net/uploads/img.srnyx.xyz/circle.png`
      );

    interaction.followUp({ embeds: [embed], ephemeral: true });
  },
};
