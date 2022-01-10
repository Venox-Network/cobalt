const bot = require("../index");
const config = require("../config.json");

bot.on("ready", () =>
  console.log(`${bot.user.tag} is up and ready to go! Also I like cookies :)`)
);
/*
const servers = bot.guilds.cache.size;
const servercount = bot.guilds.cache.reduce((a, b) => a + b.memberCount, 0);

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

const activities = [
  { type: "PLAYING", message: "a game" },
  { type: "WATCHING", message: "a video" },
  { type: "LISTENING", message: "a song" },
];

bot.on("ready", () => {
  setInterval(() => {
    //const state = (0 + 1) % activities.length;
    //const presence = activities[state];

    const randomIndex = Math.floor(Math.random() * (activities.length - 1) + 1);
    const newActivity = activities[randomIndex];

    bot.user.setActivity(activities[randomIndex].message, {
      type: activity[randomIndex].type,
    });
  }, 9000);
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
