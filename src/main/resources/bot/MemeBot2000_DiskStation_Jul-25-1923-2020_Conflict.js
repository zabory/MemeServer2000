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
		
		//lets make sure the bot doesnt process messages from other channels
		allowedChannel = true
		
		bot.guilds.cache.array()[0].channels.cache.array().forEach(ch => {
			if(data.channel.name == ch.name && data.channel.name != 'meme-approval'){
				allowedChannel = false;
			}
		});
		
		if(allowedChannel){
			if(data.attachments.size > 0){
				url = data.attachments.array()[0].url
			
				//message content should end up being the tags
				tags = data.content
				if(tags != ""){
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
				
					json = {'user':user, 'admin':adminRole, 'channelID':channel, 'url':url, 'tags': tags}
					console.log(JSON.stringify(json))
				
					if(json.admin){
						data.reply('Meme submitted to the database')
					}else{
						data.reply('Meme has been submitted to be revied by a curator')
					}
				
				}else{
					data.reply('Give me tags! Give me taaaags!')
				}
			
			}else{
				data.reply('I dont see a meme here??')
			}
		}
	}
});

//Input to program
consoleInput.on('line', (input) => {
	
	//I expect this to have some stuff
	//json = JSON.parse(input)
	user= json.username
	command = json.command
	channelID = json.channelID
	
	
	bot.users.cache.array().forEach(currentUser => {
		if(user == currentUser.username){
			currentUser.createDM().then(userDM =>{
				userDM.send('UwU')
			});
		}
	});
});

bot.login(auth.token)