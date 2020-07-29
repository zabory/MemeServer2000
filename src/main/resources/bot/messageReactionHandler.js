var auth = require('./auth.json');

module.exports = {
		handle : function(data){
			
			//we need this because the bot adds the reactions on to the picture the first time, so only do this if more than the bot reacts
			if(data.users.cache.array().length > 1){
				user = data.author.username
				// get the channel ID
				channel = data.message.channel.id
				//make sure the channelID is the meme curator channel
				if(channel == auth.channel){
					if(data.emoji.id == auth.deny){
						json = {'command':'deny', 'user':user}
						console.log(JOSN.stringify(json))
					}else if(data.emoji.id == auth.approve){
						json = {'command':'approve', 'user':user}
						console.log(JOSN.stringify(json))
					}
				}
			}
		}
}