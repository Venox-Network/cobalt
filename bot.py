import asyncio
import nextcord
import orjson
import os
import wavelink
from nextcord.ext import commands

try:
    with open("config.json", "rb") as f:
        config = orjson.loads(f.read())

        TOKEN = config["bot-token"]
        CLUSTER = config["mongodb-con"]
        Global_Report_Channel = config["global-report-channel-id"]
        Global_Log_Channel = config["global-log-channel-id"]
except Exception as e:
    print("Generating config.json...")
    with open("config.json", "w") as f:
        f.write("""{
    "bot-token": "",
    "mongodb-con": "",
    "global-report-channel-id": "",
    "global-log-channel-id": ""
}""")
    
    print("Please fill in the given details in config.json and restart the bot!")
    exit(1)

intents = nextcord.Intents.default()
intents.members = True
client = commands.Bot(intents=intents)


@client.event
async def on_ready():
    print("bot is ready")

    tasks = [["Vanadium SMP", "g"], [[f"{len(client.guilds)} servers"], "s"], ]
    ts = []
    for task in tasks:
        if task[1] == "g":
            ts.append(nextcord.Game(task[0]))
        elif task[1] == "s":
            ts.append(nextcord.Activity(type=nextcord.ActivityType.watching, name=task[0][0], ))
    while 1:
        for g in ts:
            await client.change_presence(status=nextcord.Status.online, activity=g)
            await asyncio.sleep(60)


@commands.Cog.listener()
async def on_wavelink_node_ready(node: wavelink.Node):
    print(f"node {node.identifier} is ready!!!")


async def node_connect():
    await client.wait_until_ready()
    await wavelink.NodePool.create_node(client=client,
                                        host='kerosine.darrennathanael.com',
                                        port=2124,
                                        password='SleepingOnTrains')


class Confirm(nextcord.ui.View):
    def __init__(self):
        super().__init__()
        self.value = None


async def load(extension):
    client.load_extension(f'cogs.{extension}')


async def unload(extension):
    client.unload_extension(f'cogs.{extension}')


for filename in os.listdir('./cogs'):
    if filename.endswith('.py'):
        client.load_extension(f'cogs.{filename[:-3]}')

client.run(TOKEN)
