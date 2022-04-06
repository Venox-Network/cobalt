const player = require("../../client/player");

module.exports = {
  name: "skip",
  description: "skip the current song",
  run: async (client, interaction, args) => {
    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "❌ Join a voice channel first",
      });

    if (
      interaction.guild.me.voice.channelId &&
      interaction.member.voice.channelId !==
        interaction.guild.me.voice.channelId
    ) {
      await interaction.followUp({
        content: "❌ You are not in my voice channel",
        ephemeral: true,
      });
    }

    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "❌ No music is currently being played",
      });

    //FIXME Unresolved function or method catch()
    await queue.skip().catch((err) => interaction.followUp({
      content: "❌ Could not find next song" //`❌ There was an error trying to execute that command: \`${err.message}\``
    }));

    setTimeout(function () {
      interaction.followUp({
        //content: `⏭ Playing **${queue.current.title}**`
        //FIXME Unresolved variable or type err
        content: !err ? `⏭ Playing **${queue.current.title}**` : "⏭ Skipped song",
      });
    }, 1200);
  },
  catch(error) {
    console.log(error);
    //FIXME interaction is undefined
    interaction.followUp({
      content:
        "❌ There was an error trying to execute that command: " +
        `\`${error.message}\``,
    });
  },
};
