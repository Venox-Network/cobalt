module.exports = {
  config: {
    name: "lennyface",
    description: "Get a random lenny face!",
    aliases: ["face", "lenny", "lf"],
    usage: "<user>",
    category: "fun",
    accessableby: "Member"
  },

  run: async (bot, message, args) => {
    const user = message.mentions.users.first() || message.author;

    const responses = [
      `( ͡° ͜ʖ ͡°)`,
      `(▀̿Ĺ̯▀̿ ̿)`,
      `¯\\_(ツ)_/¯`,
      `ಠ_ಠ`,
      `ʕ•ᴥ•ʔ`,
      `༼ つ ◕_◕ ༽つ`,
      `(づ｡◕‿‿◕｡)づ`,
      `(ಥ﹏ಥ)`,
      `( ͠° ͟ʖ ͡°)`,
      `(ง'̀-'́)ง`,
      `┬┴┬┴┤ ͜ʖ ͡°) ├┬┴┬┴`,
      `(͡ ͡° ͜ つ ͡͡°)`,
      `﴾͡๏̯͡๏﴿`,
      `(ᵔᴥᵔ)`,
      `(¬‿¬)`,
      `(づ￣ ³￣)づ`,
      `ლ(ಠ益ಠლ)`,
      `(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧`,
      `(•_•)`
    ];

    message.channel.send(responses.random());
  }
};
