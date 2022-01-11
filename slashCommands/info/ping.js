const { Client, CommandInteraction, MessageEmbed } = require("discord.js");

module.exports = {
    name: "ping",
    description: "returns websocket ping",
    type: 'CHAT_INPUT',
    /**
     *
     * @param {Client} client
     * @param {CommandInteraction} interaction
     * @param {String[]} args
     */
    run: async(client, interaction, args) => {
        interaction.followUp("Pinging...");
        const reply = await interaction.fetchReply();
		const embed = new MessageEmbed()
			.setTitle("Pong!")
			.addField("💓 Heartbeat", `${client.ws.ping}ms`)
			.addField("🔁 Roundtrip", `${reply.createdTimestamp - interaction.createdTimestamp}ms`);
		reply.edit({ content: " ", embeds: [embed] });
    },
};