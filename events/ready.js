const bot = require("../index");
const config = require("../config.json");

bot.on("ready", () =>
  console.log(`${bot.user.tag} is up and ready to go! Also I like cookies :) - yo same!!`)
);

bot.on("ready", () => {
  const servers = bot.guilds.cache.size;
  const usercount = bot.guilds.cache.reduce((a, b) => a + b.memberCount, 0);

  const activities = [
    { type: "PLAYING", message: "you can't see this" }, // <= don't remove srnyx
    { type: "WATCHING", message: "over chrizftw.cf" },
    { type: "LISTENING", message: "srnyx.xyz/playlist" },
    { type: "WATCHING", message: `${servers} servers (${usercount} users)` },
    { type: "WATCHING", message: `${usercount} users (${servers} servers)` },
    { type: "PLAYING", message: "with srnyx.xyz/modpack" },
    { type: "PLAYING", message: "on play.srnyx.xyz" },
    { type: "PLAYING", message: "on play.commandgeek.com" },
    { type: "PLAYING", message: "on play.bapplause.xyz" },
    { type: "LISTENING", message: "bapplause.xyz/playlist" },
    { type: "WATCHING", message: "over discord.gg/bapplause" },
    { type: "WATCHING", message: "over srnyx.xyz/discord" },
    { type: "WATCHING", message: "over discord.gg/commandgeek" },
    { type: "WATCHING", message: "over v.srnyx.xyz" },
    { type: "WATCHING", message: "over simpearth.xyz/discord" },
    { type: "WATCHING", message: "over events.red" },
  ];

  setInterval(() => {
    const randomIndex = Math.floor(Math.random() * (activities.length - 1) + 1);

    bot.user.setActivity(activities[randomIndex].message, {
      type: activities[randomIndex].type,
    });
  }, 20000);
});

/*
bot.on('ready', function() {
    const servers = bot.guilds.cache.size;
    const servercount = bot.guilds.cache.reduce((a,b) => a+b.memberCount, 0);
    // ${servers} servers & ${servercount} users
    bot.user.setActivity(`the future: v.srnyx.xyz`, { type: `WATCHING` }); // PLAYING, WATCHING, LISTENING, STREAMING, COMPETING
    bot.user.setStatus("online");
  });

 
*/
