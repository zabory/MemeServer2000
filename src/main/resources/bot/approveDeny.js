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
			
			//add admin tags to list
			if(addedTags){
				json[approvedTags] = json.approvedTags + ',' + tags
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