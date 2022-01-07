const discordTogether = require("../../client/discordTogether");
const { CommandInteraction, Client, Message, MessageActionRow, MessageButton } = require("discord.js");

module.exports = {
  name: "yt",
  description: "watch youtube together on discord",
  options: [
    {
      name: "channel",
      description: "channel to watch youtube on",
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
    const channel = interaction.guild.channel.cache.get(channelID);

    if (channel.type !== "GUILD_VOICE")
      return interaction.followUp({ content: "Please choose a voice channel!" });

      
      discordTogether
      .createTogetherCode(channelID, "youtube")
      .then((x) => interaction.followUp(x.code));
      
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
