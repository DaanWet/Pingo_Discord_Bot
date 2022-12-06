# Pingo_Discord_Bot

Pingo is a fun/moderation bot including blackjack, uno, role picker and some more.
[Invite me](https://discord.com/api/oauth2/authorize?client_id=589027434611867668&permissions=413873335536&scope=bot) to your server!

## Fun Commands

### Casino Commands

- `!daily` : Collect your daily credits
- `!weekly` : Collect your weekly credits
- `!blackjack <bet>`: Start a blackjack game with betting a given amount of credits. (When a game started for you use !hit, !stand, !double and !split to alter the outcome)
- `!balance` : Show your balance (or use `!bal top` to see the 10 members with the most credits in your server)
- `!records` : Show the records (most credits won, most credits lost,...)

### Uno
Use `!uno` to set up a uno game, react with 🖐️ to join the game and react with ▶️ to start the game.

More info coming soon 

## Moderation Commands

### Role Picker

- `!addRoleAssign <category> <emoji> <role> <name>` : Add a role to the \<category> role picker with an emoji, role and display name
- `!removeRoleAssign <category> <emoji> : Remove the role assign corresponding to the emoji in the \<category>
- `!roleassign <category>` : Show/move the role picker for the \<category> to the current channel

More info coming soon

# Other features
## Upcoming new features
- Uno turn reminders
- Uno messaging
- Pin message command
- Blackbox
- Temporary voice channel command
- Never have I ever
- Leveling
- Poker
- More casino games
- Reminder
- Balance lookup

## Upcoming improvements
- More enhanced poll
- Better Among us Command
- Slash commands

## Scrapped features
- Once slash commands are implemented most command settings will be deleted

# Contributing

If you want to contribute to Pingo, make sure to base your branch off of the development branch and create your PR into that same branch. Any PRs between branches or into the master branch will be rejected! It is very possible that your change might already be in development or you missed something.

# Self hosting this bot

## Requirements
- Java 14 or newer
- Maven
- MySQL Database
- Github repository

## Setup
- Build the bot using maven either using your preffered IDE or command line
- Create a Discord application in the discord developer portal
- Create a database in your MySQL instance and create a user with a password
- Copy the jdbc connection url of the database with utf8mb4 as the character set of the server.
- Fill in the bot token, github token and jdbc connection url in the `config.properties` file
- Fill in any other settings you want to change in the `config.properties` file
- Run the bot (wether or not you use nohup, screen or any other kind of service is up to you)

