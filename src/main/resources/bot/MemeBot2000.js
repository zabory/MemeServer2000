var Discord = require('discord.js');
var logger = require('winston');
var readline = require('readline');

var auth = require('./auth.json');

var MRH = require('./messageReactionHandler.js')
var OMH = require('./onMessageHandler.js')
var CIH = require('./consoleInputHandler.js')

var bot = new Discord.Client();

const consoleInput = readline.createInterface({
	input: process.stdin,
	output: process.stdout
});

// set activity of the bot
bot.on('ready', () => {
	bot.user.setActivity("Someone get this man a meme")
});

// whenever the bot gets a message
bot.on('message', data => {
	OMH.handle(bot, data)
});

// whenever the bot sees a reaction to a message
bot.on('messageReactionAdd', (data, userdata) => {
	MRH.handle(data, userdata)
});

// Input to program from server
consoleInput.on('line', input => {
	CIH.handle(bot, input)
});

bot.login(auth.token)