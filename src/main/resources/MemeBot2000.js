var Discord = require('discord.js');
var logger = require('winston');
var auth = require('./auth.json');

var bot = new Discord.Client();

bot.on('ready', () => {
	bot.user.setActivity("Someone get this man a meme")
});

bot.on('message', data => {
	user = data.author.username
	if(user != 'MemeBot2000'){
		channel = data.channel.id
		if(data.attachments.size > 0){
			url = data.attachments.array()[0].url
			tags = data.content
			console.log(user + "," + channel + "," + url + "," + tags)
		}else{
			data.reply('Please attach a meme next time')
		}
	}
});

bot.login(auth.token)