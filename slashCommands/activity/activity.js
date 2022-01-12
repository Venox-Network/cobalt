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
      type: "STRING",
      required: true,
      choices: [
        {
          name: "YouTube Together",
          value: "yt",
        },
        {
          name: "Doodle Crew",
          value: "dc",
        },
        {
          name: "Fishington",
          value: "fish",
        },
      ],
    },
    {
      name: "channel",
      description: "channel to start the activity",
      type: "CHANNEL",
      required: true,
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
    const [channelID] = args[2];
    const channel = interaction.guild.channels.cache.get(channelID);

    if (channel.type !== "GUILD_VOICE")
      return interaction.followUp({
        content: "Please choose a voice channel!",
      });

    const value = interaction.options.get("game").value;

    if (value === "yt") {
      discordTogether
        .createTogetherCode(channelID, "youtube")
        .then((x) => interaction.followUp(x.code));
    } else if (value === "dc") {
      discordTogether
        .createTogetherCode(channelID, "doodlecrew")
        .then((x) => interaction.followUp(x.code));
    } else if (value === "fish") {
      discordTogether
        .createTogetherCode(channelID, "fishing")
        .then((x) => interaction.followUp(x.code));
    }

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
