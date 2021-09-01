module.exports = {
  config: {
    name: 'ping',
    description: 'Get the bot and API latency.',
    aliases: ['p'],
    usage: 'v!\`p\`',
    category: 'fun',
    accessableby: 'anyone'
  },
  run: async (bot, message, args) => {
    const msg = await message.channel.send('Pinging...');

    const ping = msg.createdTimestamp - message.createdTimestamp;

    msg.edit(`PingPong sucks but ok :p\n**Bot latency is ${ping}ms, API latency is ${bot.ws.ping}ms.**`);

  }
}

