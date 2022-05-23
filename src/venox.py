import discord
from config import Config
from discord_bot import Bot
from src.cogs.all_super_commands import super_ban, super_unban, super_kick
from src.cogs.fun import music_commands, react
from src.cogs.information import servers, support
from src.cogs.moderation_commands import nick_name_commands, sticky_message, slowmode_commands, \
    report_commands, auto_thread, ban, all_warns, kick, leave_server, mute, purge, unmute
from src.cogs.time_loops import time_jobs


def main():
    main_config: Config = Config.get_conf_from_file()

    intents = discord.Intents.default()
    intents.members = True
    intents.message_content = True

    main_bot: Bot = Bot(conf=main_config, intents=intents)

    main_bot.add_cogs(
        # superban commands
        super_ban.cog_creator,
        super_unban.cog_creator,
        super_kick.cog_creator,
        # fun commands
        music_commands.cog_creator,
        react.cog_creator,
        # information commands
        servers.cog_creator,
        support.cog_creator,
        # moderation commands
        auto_thread.cog_creator,
        nick_name_commands.cog_creator,
        report_commands.cog_creator,
        slowmode_commands.cog_creator,
        sticky_message.cog_creator,
        ban.cog_creator,
        all_warns.cog_creator,
        kick.cog_creator,
        leave_server.cog_creator,
        mute.cog_creator,
        purge.cog_creator,
        unmute.cog_creator,
        # time jobs
        time_jobs.cog_creator,
    )

    main_bot.run(main_config.BOT_TOKEN)


if __name__ == '__main__':
    main()
