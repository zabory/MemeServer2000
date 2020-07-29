var auth = require('./auth.json');

module.exports = {
		
		handle: function(bot, input){
			
			// I expect this to have some stuff
			json = JSON.parse(input)
			user = json.user
			command = json.command
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
				channelID = json.channelID
	
				//go through userDM channel IDs as well to get the right channel
				message = bot.channels.cache.get(channelID)
				
				if(message == null){
					bot.users.cache.array().forEach(currentUser => {
						if(currentUser.dmChannel == channelID){
							message = currentUser.dmChannel
						}
					});
				}
				
				// sends message
				message.send(body).then(messageT => {
					if(command == 'sendToQueue'){
						// add reactions
						messageT.react(message.guild.emojis.cache.get(auth.approve))
						messageT.react(message.guild.emojis.cache.get(auth.deny))
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