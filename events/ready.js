const bot = require("../index");
const config = require("../config.json");

bot.on("ready", () =>
  console.log(`${bot.user.tag} is up and ready to go! Also I like cookies :)`)
);
/*

const activities = [
  "the future.",
  "srnyx's Modpack: srnyx.xyz",
  "Chriz's Graphics: chrizftw.cf",
  `${servers} servers & ${servercount} users`,
];


bot.on("ready", () => {
  // run every 10 seconds
  setInterval(() => {
    // generate random number between 1 and list length.
    const randomIndex = Math.floor(Math.random() * (activities.length - 1) + 1);
    const newActivity = activities[randomIndex];
    bot.user.setActivity(newActivity, { type: `WATCHING` });
    bot.user.setStatus("online");
  }, 10000);
});
*/

const servers = bot.guilds.cache.size;
const usercount = bot.guilds.cache.reduce((a, b) => a + b.memberCount, 0);

const activities = [
  { type: "PLAYING", message: "you can't see this" },
  { type: "WATCHING", message: "over chrizftw.cf" },
  { type: "LISTENING", message: "srnyx.xyz/playlist" },
  { type: "WATCHING", message: `${servers} servers` },
  { type: "WATCHING", message: `${usercount} servers` },
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

bot.on("ready", () => {
  setInterval(() => {
    //const state = (0 + 1) % activities.length;
    //const presence = activities[state];

    const randomIndex = Math.floor(Math.random() * (activities.length - 1) + 1);
    //const newActivity = activities[randomIndex];

    bot.user.setActivity(activities[randomIndex].message, {
      type: activities[randomIndex].type,
    });
  }, 10000);
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
