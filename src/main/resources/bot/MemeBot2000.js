var Discord = require('discord.js');
var logger = require('winston');
var auth = require('./auth.json');
// // Configure logger settings
// logger.remove(logger.transports.Console);
// logger.add(logger.transports.Console, {
//     colorize: true
// // });
// logger.level = 'debug';
// Initialize Discord Bot
var bot = new Discord.Client();
bot.on('ready', function (evt) {
  //   var lastMsg = bot.channels.get(auth.channel).fetchMessages()
  // .then(messages => `${messages.filter(m => m.author.id === '674409301409923093').first().delete()} messages`);
  // console.log(lastMsg);
});
bot.on('message', message => {
	console.log(message.content);
    if (message.content.length > 4 &&
    	message.content.substring(0, 4) == '!RIP'){
    	var name = message.content.substring(4);

    	var msg = 'RIP ' + name
    	console.log(msg);
        message.delete();
        bot.channels.get(auth.channel).send(msg);
	}

});

bot.login(auth.token)