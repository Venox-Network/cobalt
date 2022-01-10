const discordTogether = require("../../client/discordTogether");
const {
  CommandInteraction,
  Client,
  Message,
  MessageActionRow,
  MessageButton,
} = require("discord.js");

module.exports = {
  name: "activity-betrayal",
  description: "play betrayal.io together on discord",
  options: [
    {
      name: "channel",
      description: "channel to play betrayal.io on",
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

    discordTogether
      .createTogetherCode(channelID, "betrayal")
      .then((x) => interaction.followUp(x.code));

  },
};
