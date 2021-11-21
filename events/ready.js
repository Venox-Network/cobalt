const bot = require("../index");

bot.on("ready", () =>
    console.log(`${bot.user.tag} is up and ready to go!`)
);
