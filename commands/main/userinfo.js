const { MessageEmbed } = require('discord.js');
const { Attachment } = require("discord.js");

module.exports = {
  config: {
    name: "userinfo",
    description: "Get info of a user.",
    aliases: ["user"],
    usage: "<user>",
    category: "main",
    accessableby: "Members"
 },

  run: async (bot, message, args) => {
    let color = message.member.displayHexColor;
if (color == '#000000') color = message.member.hoistRole.hexColor;
    const user = message.mentions.users.first() || bot.users.cache.get(args[0]) || message.author; // message.author; //message.guild.members.get()
    if(!user) return message.channel.send('Couldn\'t find that user :(');
    const member = message.guild.member(user);
      const embed = new MessageEmbed()
        .setColor(color)
        .setAuthor(user.tag)
        .setDescription(`${user}`)
        .setThumbnail(user.displayAvatarURL)
       	.addField('Join Date:', `${member.joinedAt}`, true)
        .addField("Account Creation Date:", member.user.createdAt, true)
        .addField("Nickname:", `${member.nickname !== null ? `${member.nickname}` : 'None'}`, true)
        .addField("Roles:", member.roles.cache.map(roles => `${roles}`).join(', '), true)
        .addField("Custom Status:", `${user.presence.game ? user.presence.game.name : 'None'}`, true)
        .addField("Status:", `${user.presence.status}`, true)
        .addField('User\'s ID:', user.id, true)

        .setFooter(`© ${message.guild.me.displayName} | Guild ID: ${message.guild.id}`);

      message.channel.send(embed);


   }
};
