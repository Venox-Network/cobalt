const bot = require("../index");
const config = require("../config.json");

bot.on("ready", () =>
    console.log(`${bot.user.tag} is up and ready to go! Also I like cookies :)`)
);

bot.on('ready', function() {
    bot.user.setActivity(config.activity, { type: `CUSTOM_STATUS` });
    bot.user.setStatus("dnd");
  });



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