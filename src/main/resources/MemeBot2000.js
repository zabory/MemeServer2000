var Discord = require('discord.js');
var logger = require('winston');
var auth = require('./auth.json');

var bot = new Discord.Client();

bot.on('ready', () => {
	bot.user.setActivity("Someone get this man a meme")
});

bot.on('message', data => {
	console.log('heckin log dude')
});

bot.login(auth.token)