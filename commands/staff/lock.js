const Discord = require("discord.js");

module.exports = {
  config: {
    name: 'lock',
    description: 'Lock a channel',
    aliases: ['channellock'],
    usage: '<user>',
    category: 'staff',
    accessableby: 'Staff'
  },



run: async (bot, message, args) => {

 	const channel = message.channel;
        await channel.overwritePermissions('everyone', {'SEND_MESSAGES': false}, 'just because lol')
 	await message.channel.send(`${message.channel} is now locked`);

    // handle responses / errors
    


}
/*
// find specific role - enter name of a role you create here
    let testRole = message.guild.roles.find('name', '<name of role>');

    // overwrites 'SEND_MESSAGES' role, only on this specific channel
    channel.overwritePermissions(
        testRole,
        { 'SEND_MESSAGES': false },
        // optional 'reason' for permission overwrite
        'closing up shop'
    )
*/
}