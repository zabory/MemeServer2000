module.exports = {
		
		/**
		 * addedTags: boolean if we are adding tags or not
		 * tags: tags to be added
		 */
		handle: function(bot, addedTags, tags, user){
			
			json = {'command':'approve', 'approvedTags' : '', 'user':user}
			
			
			
			/**
			 * TODO
			 * ====
			 * go through tags that got sent with meme, match them up with the react numbers
			 * if a tag got reacted to, then dont add it to tag list
			 * add admin tags to list
			 * send it
			 */
			
			//go through tags that got sent with meme
			//if a tag got reacted to, dont add it to the list
			bot.guilds.cache.array()[0].channels.cache.array().forEach(ch => {
				if(ch.name == 'meme-approval'){
					ch.messages.cache.array()[2].reactions.cache.array().forEach(reaction =>{
						if(reaction.count < 2){
							if(reaction.emoji.name == 'one'){
								number = 1
							}else if(reaction.emoji.name == 'two'){
								number = 2
							}else if(reaction.emoji.name == 'three'){
								number = 3
							}else if(reaction.emoji.name == 'four'){
								number = 4
							}else if(reaction.emoji.name == 'five'){
								number = 5
							}else if(reaction.emoji.name == 'six'){
								number = 6
							}else if(reaction.emoji.name == 'seven'){
								number = 7
							}else if(reaction.emoji.name == 'eight'){
								number = 8
							}else if(reaction.emoji.name == 'nine'){
								number = 9
							}else if(reaction.emoji.name == 'ten'){
								number = 10
							}else{
								number = 11
							}
							
							if(number != 11){
								tagsMessage = ch.messages.cache.array()[1]
								json.approvedTags = json.approvedTags + ',' + tagsMessage.content.split('\n')[number].replace(number + ': ', '')
							}
						}
					});
				}
			});
			
			//add admin tags to list
			if(addedTags == true){
				json.approvedTags = json.approvedTags + ',' + tags
			}
			
			if(json.approvedTags.charAt(0) == ','){
				json.approvedTags = json.approvedTags.replace(',', '')
			}
			
			//send it
			console.log(JSON.stringify(json))
		}
}


/**
 * command : 'approve'
 * approvedTags : comma delimited list of approved tags
 * user: user who approved the meme
 */

//json = {'command':'deny', 'user':userData.username}
//console.log(JSON.stringify(json))
//}else if(data.emoji.name == 'check'){
//json = {'command':'approve', 'user':userData.username}
//console.log(JSON.stringify(json))
//}