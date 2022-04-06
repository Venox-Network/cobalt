
const player = require("../../client/player");
const axios = require("axios");
const { MessageEmbed } = require("discord.js");
const client = require("../../index");

const getLyrics = (title) =>
    new Promise(async (ful, rej) => {
        const url = new URL("https://some-random-api.ml/lyrics");
        url.searchParams.append("title", title);

        try {
            const data = await axios.get(url.href);
            ful(data);
        } catch (error) {
            rej(error);
        }
    });

const substring = (length, value) => {
    const replaced = value.replace(/\n/g, "--");
    const regex = `.{1,${length}}`;
    return replaced
        .match(new RegExp(regex, "g"))
        .map((line) => line.replace(/--/g, "\n"));
};

const createResponse = async (title) => {
    try {
        const data = await getLyrics(title);
        //FIXME lyrics is undefined
        return substring(4096, data.lyrics).map((value, index) => {
            const isFirst = index === 0;
            return new MessageEmbed({
                title: isFirst ? `${data.title} - ${data.author}` : null,
                //FIXME genius is undefined
                thumbnail: isFirst ? {url: data.thumbnail.genius} : null,
                description: value,
                color: "0070c0",
                footer: {
                    text: "Venox Music",
                    icon_url: client.user.displayAvatarURL()
                },
            });
        });
    } catch (error) {
        return "❌ | I am not able to find lyrics for this song";
    }
};

module.exports = {
    name: "lyrics",
    description: "display lyrics for the current song or a specific song",
    options: [
        {
            name: "title",
            description: "specific song for lyrics",
            type: "STRING",
            required: false
        }
    ],
    run: async (client, interaction) => {
        if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "❌ | Join a voice channel first",
      });

    if (
      interaction.guild.me.voice.channelId &&
      interaction.member.voice.channelId !==
        interaction.guild.me.voice.channelId
    ) {
      await interaction.followUp({
          content: "❌ | You are not in my voice channel",
          ephemeral: true,
      });
    }

        const title = interaction.options.getString("title");
        const sendLyrics = (songTitle) => {
            return createResponse(songTitle)
                .then((res) => {
                    console.log(res);
                    interaction.followUp(res);
                })
                .catch((err) => console.log(err));
        };

        if (title) return sendLyrics(title);

        const queue = player.getQueue(interaction.guildId);
        if (!queue?.playing)
            return interaction.followUp({
                content: "❌ | No music is currently being played"
            });

        return sendLyrics(queue.current.title);
    },
    catch(error) {
        console.log(error);
        //FIXME interaction is undefined
        interaction.followUp({
          content:
            "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
        });
      },
};