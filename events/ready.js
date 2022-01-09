const bot = require("../index");
const config = require("../config.json");

bot.on("ready", () =>
  console.log(`${bot.user.tag} is up and ready to go! Also I like cookies :)`)
);

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

/*
bot.on('ready', function() {
    const servers = bot.guilds.cache.size;
    const servercount = bot.guilds.cache.reduce((a,b) => a+b.memberCount, 0);
    // ${servers} servers & ${servercount} users
    bot.user.setActivity(`the future: v.srnyx.xyz`, { type: `WATCHING` }); // PLAYING, WATCHING, LISTENING, STREAMING, COMPETING
    bot.user.setStatus("online");
  });

 
*/
/*
bot.on("ready", async() => {
    
    const servers = await bot.guilds.cache.size;
    const servercount = await bot.guilds.cache.reduce((a,b) => a+b.memberCount, 0);

    
    bot.user.setPresence({
        status: 'dnd',
        activity: {
            name: 'a video',
            type: 'WATCHING'
         }
      });
    
    
    const messages = [
        `Owned by srnyx & ChrizxzFTW`,
        `Join the network: dsc.gg/venoxnet`,
        `Watching ${servers} servers and ${servercount} members!`
    ]

    setInterval(() => {
        const status = messages[Math.floor(Math.random()*messages.length)]
        bot.user.setPresence({ messages : [{name : `${status}`}]})
    }, 5000);
    
  });
  */
