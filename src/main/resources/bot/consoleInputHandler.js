

module.exports = {
		
		handle: function(bot, input, auth){
			
			// I expect this to have some stuff
			json = JSON.parse(input)
			command = json.command
			
			// sends message to user
			if(command == 'sendToUser'){
				user = json.user
				body = json.body
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
				body = json.body
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
			}else if(command == 'sendAllTags'){
				tagList = json.body
				helpChannel = bot.channels.cache.get(auth.helpChannel)
				
				helpChannel.messages.cache.array().forEach(currentMessage => {
					if(currentMessage.content.includes('Tag list')){
						currentMessage.edit('Tag list\n=================\n' + tagList.replace(',', '\n'))
					}
				});
				
			}else if(command == 'queueSize'){
				queueSize = json.body
				helpChannel = bot.channels.cache.get(auth.channel)
				
				helpChannel.messages.cache.array().forEach(currentMessage => {
					if(currentMessage.content.includes('Queue size ')){
						currentMessage.edit('Queue size ' + queueSize)
					}
				});
				
			}else if(command == 'sendAllCommands'){
				tagList = json.body
				helpChannel = bot.channels.cache.get(auth.helpChannel)
				
				helpChannel.messages.cache.array().forEach(currentMessage => {
					if(currentMessage.content.includes('Command list')){
						currentMessage.edit('Command list\n=================\n' + tagList.replace(',', '\n'))
					}
				});
				
			}else if(command == 'clearHelpChannel'){
				helpChannel = bot.channels.cache.get(auth.helpChannel)
				helpChannel.bulkDelete(100)
				helpChannel.send('Tag list\n=================\n')
				helpChannel.send('Command list\n=================\n')
			}
			
			
		}
}