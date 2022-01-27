const player = require("../../client/player");
const { GuildMember } = require("discord.js");
const { QueueRepeatMode } = require("discord-player");
const { Client, CommandInteraction } = require("discord.js");

module.exports = {
  name: "loop",
  description: "sets loop mode",
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   */
  options: [
    {
      name: "mode",
      type: "INTEGER",
      description: "loop type",
      required: true,
      choices: [
        {
          name: "Off",
          value: QueueRepeatMode.OFF,
        },
        {
          name: "Song",
          value: QueueRepeatMode.TRACK,
        },
        {
          name: "Queue",
          value: QueueRepeatMode.QUEUE,
        },
        /*{
          name: "Autoplay",
          value: QueueRepeatMode.AUTOPLAY,
        },*/
      ],
    },
  ],
  run: async (client, interaction, args) => {
    

    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "❌ | Join a voice channel first",
      });

    if (
      interaction.guild.me.voice.channelId &&
      interaction.member.voice.channelId !==
        interaction.guild.me.voice.channelId
    ) {
      interaction.followUp({
        content: "❌ | You are not in my voice channel",
        ephemeral: true,
      });
    }

    const channel = interaction.member.voice.channel;
    if (channel) {
      const queue = player.getQueue(interaction.guildId);
      if (!queue || !queue.playing) {
        return void interaction.followUp({
          content: "❌ | No music is being played",
        });
      }

      const loopMode = interaction.options.get("mode").value;
      const loopName =
        loopMode === QueueRepeatMode.TRACK
          ? "Song"
          : loopMode === QueueRepeatMode.QUEUE
          ? "Queue"
          : "Off";
      const success = queue.setRepeatMode(loopMode);
      const mode =
        loopMode === QueueRepeatMode.TRACK
          ? "🔂"
          : loopMode === QueueRepeatMode.QUEUE
          ? "🔁"
          : "▶";

      return void interaction.followUp({
        content: success
          ? `${mode} | Looping ${loopName}`
          : "❌ | Could not update loop mode",
      });
    } else {
      interaction.followUp({
        content: "❌ | I'm not connected to a voice channel",
      });
    }
  },
  catch(error) {
    console.log(error);
    interaction.followUp({
      content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};
