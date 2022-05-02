const client = require("../index.js");

client.on("interactionCreate", async (interaction) => {
  // Slash Command Handling
  if (interaction.isCommand()) {
    //FIXME Unresolved function or method
    await interaction.deferReply().catch(() => {});

    //FIXME Unresolved variable name
    const cmd = client.slashCommands.get(interaction.commandName);
    if (!cmd)
        //FIXME Unresolved function or method
      return interaction.followUp({
        content: "Uh oh.... srnyx broke the bot.. ",
        ephemeral: true,
      });

    const args = [];

    //FIXME Unresolved function or method
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

    if (!interaction.member.permissions.has(cmd.userPermissions || []))
        //FIXME Unresolved function or method
      return interaction.followUp({
        content: "Your lacking permissions to use this command",
        ephemeral: true,
      });
      if (!interaction.guild.me.permissions.has(cmd.botPermissions || []))
          //FIXME Unresolved function or method
      return interaction.followUp({
        content: "I lack permissions to use this command",
        ephemeral: true,
      });

    cmd.run(client, interaction, args);
  }

  // Context Menu Handling
  if (interaction.isContextMenu()) {
    //FIXME Unresolved function or method
    await interaction.deferReply({ephemeral: true});
    //FIXME Unresolved variable name
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
