# venox-rewrite

<hr>

Steps on how to start bot:
1. Install `pie` version according to your OS from [PIE-RUST](https://github.com/skandabhairava/pie-rust/releases/tag/main)
2. Rename the downloaded executable to just "pie", and place it in a directory which is inside of your %PATH% for ease of access. 
3. Clone this repo, and cd into it
4. `pie run`, the first time you run it, it will need time to start up a virtual environment and download the required dependencies.
5. Run the above command once again, and the bot *should* crash asking you to fill up the config. Fill up 'config.json' now generated in src/
6. Run the command once again to start the bot. From now onwards, just running `pie run` should start the bot.

<hr>

NOTE: As the PIE project is being rewritten from python to RUST. I will be making a CLI tool to update/download the latest PIE manager fomr github.\
After thats written, you can skip steps 1 and 2, it *should* be as easy as `pip install pie-manager`(pie-manager is being rewritten, please dont install this yet*), and `pie-manager update`

<hr>