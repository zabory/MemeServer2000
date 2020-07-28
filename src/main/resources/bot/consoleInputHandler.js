var auth = require('./auth.json');

module.exports = {
		
		handle: function(bot, input){
			
			// I expect this to have some stuff
			json = JSON.parse(input)
			
			user = json.user
			command = json.command
			channelID = json.channelID
			body = json.body
			
			// sends message to user
			if(command == 'sendToUser'){
				// find and open user dm
				bot.users.cache.array().forEach(currentUser => {
					if(user == currentUser.username){
						currentUser.createDM().then(userDM => {
							// send the message
							userDM.send(body)
						});
					}
				});
			// sends message to meme channel
			}else if(command == 'sendToChannel' || command == 'sendToQueue'){
				// sends message
				message = bot.channels.cache.get(channelID).send(body).then(message => {
					if(command == 'sendToQueue'){
						// add reactions
						message.react(message.guild.emojis.cache.get(auth.approve))
						message.react(message.guild.emojis.cache.get(auth.deny))
					}
					
				});
				
				
			// clears queue of meme channel
			}else if(command == 'clearQueue'){
				bot.guilds.cache.array()[0].channels.cache.array().forEach(channel => {
					if(channel.type == 'text' && channel.id == auth.channel){
						channel.bulkDelete(100)
					}
				});
			}
			
		}
}