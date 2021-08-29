const { RichEmbed } = require('discord.js');
const { Attachment } = require("discord.js");

module.exports = {
  config: {
    name: "serverinfo",
    description: "Get info of a server.",
    aliases: ["server"],
    usage: "<server>",
    category: "main",
    accessableby: "Members"
 },

  run: async (bot, message, args) => {
    const user = message.mentions.users.first() || message.guild.members.get(args[0]); //message.author
    const member = message.guild.member(user);
      const embed = new RichEmbed()
        .setColor("0087ef")
        .setAuthor(message.guild.name, message.guild.iconURL)
        .addField("Name:", message.guild.name, true)
        .addField("ID:", message.guild.id, true)
        .addField("Owner:", `${message.guild.owner.user.username}#${message.guild.owner.user.discriminator}`, true)
        .addField("Region:", message.guild.region, true)
        .addField("Total | Humans | Bots", `${message.guild.members.size} | ${message.guild.members.filter(member => !member.user.bot).size} | ${message.guild.members.filter(member => member.user.bot).size}`, true)
        .addField("Channels:", message.guild.channels.size, true)
        .addField("Roles:", message.guild.roles.size, true)
        .addField("Voice Channels",message.guild.channels.filter(c => c.type === 'voice').size ,true)
        .addField("Creation Date:", `${message.channel.guild.createdAt.toUTCString().substr(0, 16)}`, true)
        .setThumbnail(message.guild.iconURL)
        .setFooter(`© ${message.guild.me.displayName}`);

      message.channel.send(embed);


   }
};
