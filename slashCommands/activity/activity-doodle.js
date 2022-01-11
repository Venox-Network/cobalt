const discordTogether = require("../../client/discordTogether");
const {
  CommandInteraction,
  Client,
  Message,
  MessageActionRow,
  MessageButton,
} = require("discord.js");

module.exports = {
  name: "activity-doodle",
  description: "play doodle crew together on discord",
  options: [
    {
      name: "channel",
      description: "channel to play doodle crew on",
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
      .createTogetherCode(channelID, "doodlecrew")
      .then((x) => interaction.followUp(x.code));
  },
};
