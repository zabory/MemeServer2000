var Discord = require('discord.js');
var logger = require('winston');
var auth = require('./auth.json');
var readline = require('readline');

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

bot.on('message', data => {
	
	OMH.handle(bot, data)
	
});

bot.on('messageReactionAdd', data => {
	
	MRH.handle(data)
	
});

// Input to program
consoleInput.on('line', input => {
	
	CIH.handle(bot, input)
	
});

bot.login(auth.token)