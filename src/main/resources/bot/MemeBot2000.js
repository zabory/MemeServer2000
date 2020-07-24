var Discord = require('discord.js');
var logger = require('winston');
var auth = require('./auth.json');
var readline = require('readline');

var bot = new Discord.Client();

const consoleInput = readline.createInterface({
	input: process.stdin,
	output: process.stdout
});

//set activity of the bot
bot.on('ready', () => {
	bot.user.setActivity("Someone get this man a meme")
});

bot.on('message', data => {
	//get username of sent message
	user = data.author.username
	if(user != 'MemeBot2000'){
		//get the channel id
		channel = data.channel.id
		
		if(data.attachments.size > 0){
			url = data.attachments.array()[0].url
		}else{
			url = ""
		}
		
		//message content should end up being the tags
		tags = data.content
		
		//checks if user has the admin role
		adminRole = false
		bot.guilds.cache.array()[0].members.cache.array().forEach(member => {
			if(member.user.username == user){
				member.roles.cache.array().forEach(role => {
					if(role.name == 'Meme curator'){
						adminRole = true
					}
				});
			}
		});
		
		console.log(user + "," + adminRole + "," + channel + "," + url + "," + tags)
	}
});

//Input to program
consoleInput.on('line', (input) => {
	
});


bot.login(auth.token)