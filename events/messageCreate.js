const bot = require("../index");

bot.on("messageCreate", async (message) => {
    if (
        message.author.bot ||
        !message.guild ||
        !message.content.toLowerCase().startsWith(bot.config.prefix)
    )
        return;

    const [cmd, ...args] = message.content
        .slice(bot.config.prefix.length)
        .trim()
        .split(" ");

    const command = bot.commands.get(cmd.toLowerCase()) || bot.commands.find(c => c.aliases?.includes(cmd.toLowerCase()));

    if (!command) return;
    await command.run(bot, message, args);
});
