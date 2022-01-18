const client = require("../index.js");

client.on("interactionCreate", async (interaction) => {
  // Slash Command Handling
  if (interaction.isCommand()) {
    await interaction.deferReply({ ephemeral: false }).catch(() => {});

    const cmd = client.slashCommands.get(interaction.commandName);
    if (!cmd)
      return interaction.followUp({
        content: "Uh oh.... srnyx broke the bot.. ",
        ephemeral: true,
      });

    const args = [];

    for (let option of interaction.options.data) {
      if (option.type === "SUB_COMMAND") {
        if (option.name) args.push(option.name);
        option.options?.forEach((x) => {
          if (x.value) args.push(x.value);
        });
      } else if (option.value) args.push(option.value);
    }
    interaction.member = interaction.guild.members.cache.get(
      interaction.user.id
    );

    if (interaction.member.permissions.has(cmd.userPermissions || []))
      return interaction.followUp({
        content: "Error: Insufficient permissions",
        ephemeral: true,
      });

    cmd.run(client, interaction, args);
  }

  // Context Menu Handling
  if (interaction.isContextMenu()) {
    await interaction.deferReply({ ephemeral: true });
    const command = client.slashCommands.get(interaction.commandName);
    if (command) command.run(client, interaction);
  }

  /*
  if (interaction.isSelectMenu()) {
    interaction.reply({
      ephemeral: true,
      content: `You chose ${interaction.values[0]}`,
    });
}*/
});
