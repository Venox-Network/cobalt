const { Message, Client } = require("discord.js");

module.exports = {
    name: "ping",
    aliases: ['p'],
    /**
     *
     * @param {Client} bot
     * @param {Message} message
     * @param {String[]} args
     */
    run: async (bot, message, args) => {
        message.channel.send(`${client.ws.ping} ws ping`);
    },
};
