const Discord = require("discord.js");

module.exports = {
  config: {
    name: 'createlogs',
    description: 'Create a log channel',
    aliases: ['createlogs'],
    usage: '<user>',
    category: 'staff',
    accessableby: 'Staff'
  },



run: async (bot, message, args) => {
  if(!message.member.hasPermission('MANAGE_CHANNELS')) {console.log('ok')
                                                       } else {
const logs = message.guild.channels.find(channel => channel.name === "logs");
if (!logs)
  await message.react(`✅`);
}}
}


//✅
