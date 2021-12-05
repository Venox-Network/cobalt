const { QueryType } = require("discord-player");
const player = require("../../client/player");

const axios = require("axios");
const { MessageEmbed } = require("discord.js");

const getLyrics = (title) =>
  new Promise(async (ful, rej) => {
    const url = new URL("https://some-random-api.ml/lyrics");
    url.searchParams.append("title", title);

    try {
      const { data } = await axios.get(url.href);
      ful(data);
    } catch (error) {
      rej(error);
    }
  });

const substring = (length, value) => {
  const replaced = value.replace(/\n/g, "--");
  const regex = `.{1,${length}}`;
  const lines = replaced
    .match(new RegExp(regex, "g"))
    .map((line) => line.replace(/--/g, "\n"));

  return lines;
};

const createResponse = async (title) => {
  try {
    const data = await getLyrics(title);

    const embeds = substring(4096, data.lyrics).map((value, index) => {
      const isFirst = index === 0;

      return new MessageEmbed({
        title: isFirst ? `${data.title} - ${data.author}` : null,
        thumbnail: isFirst ? { url: data.thumbnail.genius } : null,
        description: value,
        color: "0070c0",
        footer: "Venox Music"
      });
    });

    return { embeds };
  } catch (error) {
    return "I am not able to find lyrics for this song :(";
  }
};


module.exports = {
  name: "play",
  aliases: ["p"],
  description: "play a song",
  options: [
    {
      name: "songtitle",
      description: "title of the song",
      type: "STRING",
      required: true,
    },
  ],
  run: async (client, interaction) => {
    const songTitle = interaction.options.getString("songtitle");

    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "Join a voice channel first!",
      });

    const searchResult = await player.search(songTitle, {
      requestedBy: interaction.user,
      searchEngine: QueryType.AUTO,
    });

    const queue = await player.createQueue(interaction.guild, {
      metadata: interaction.channel,
    });

    if (!queue.connection)
      await queue.connect(interaction.member.voice.channel);

    interaction.followUp({
      content: `Playing ${isFirst ? `${data.title} - ${data.author}` : null}`,
      // `Playing **${songTitle.toUpperCase}** :musical_note:`
    });

    searchResult.playlist
      ? queue.addTracks(searchResult.tracks)
      : queue.addTrack(searchResult.tracks[0]);

    if (!queue.playing) await queue.play();
  },
};
