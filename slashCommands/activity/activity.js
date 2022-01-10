const discordTogether = require("../../client/discordTogether");
const {
  CommandInteraction,
  Client,
  Message,
  MessageActionRow,
  MessageButton,
} = require("discord.js");

module.exports = {
  name: "activity",
  description: "play a select activity",
  options: [
    {
      name: "game",
      description: "activity you want to play",
      type: "INTEGER",
      choices: [
        {
          name: "youtube",
          value: "",
        }
      ]
    },
    {
      name: "channel",
      description: "channel to start the activity",
      type: "CHANNEL",
    },
  ],
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   *
   */
  run: async (client, interaction, args) => {
    const [channelID] = args;
    const channel = interaction.guild.channels.cache.get(channelID);

    if (channel.type !== "GUILD_VOICE")
      return interaction.followUp({
        content: "Please choose a voice channel!",
      });

    const value = interaction.options.get('game').value;
    

    discordTogether
      .createTogetherCode(channelID, "youtube")
      .then((x) => interaction.followUp({content: `[Click to watch YouTube!](<${x.code}>)`}));

    /*
      const row = new MessageActionRow().addComponents(
          new MessageButton()
              .setCustomId('random')
              .setLabel('success') //title of bttn
              .setStyle('SUCCESS')
              .setDisabled(false)
              .setURL(``)
      )
      */
  },
};
