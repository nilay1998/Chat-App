const express=require('express');
const socket=require('socket.io');
const mongoose = require('mongoose');
const Chat  = require("./message");
const app=express();
require('./prod')(app);


const config=require('config');

if(!config.get('db'))
{
  console.error('FATAL ERROR: Database is not defined.');
  process.exit(1);
}


mongoose.connect(config.get('db'))
  .then(() => console.log('Connected to MongoDB...'))
  .catch(err => console.error('Could not connect to MongoDB...'));


const port = process.env.PORT || 3000;
const server=app.listen(port, () => console.log(`Listening on port ${port}...`));

const io=socket(server);

app.get('/', async(req,res)=>{
    const chat = await Chat.find();
    res.send(chat);
});

io.on('connection', function(socket){
    console.log('MADE SOCKET CONNECTION');

    socket.on('join', userNickname => {
        console.log(userNickname +" : has joined the chat");
        socket.broadcast.emit('userjoinedthechat',userNickname +" : has joined the chat");
    });

    socket.on('messagedetection', async (senderNickname, messageContent) => {
        console.log(senderNickname+" : " +messageContent)
        const message = { 'message':messageContent,'senderNickname':senderNickname};
        io.emit('message',message);

        let  chatMessage  =  new Chat({ message: messageContent, sender: senderNickname});
        await chatMessage.save();
    });

    socket.on('kill', abc => {
        console.log(abc);
        socket.broadcast.emit( "userdisconnect" ,abc + " : user has left");
    });
});