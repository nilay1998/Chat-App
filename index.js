const express = require('express');
const app = express();
const bodyParser=require('body-parser');
const student=require('./routes/student');
const teacher=require('./routes/teacher');
const mongoose=require('mongoose');

require('./prod')(app);

const MongoClient = require('mongodb').MongoClient;

// replace the uri string with your connection string.
const uri = "mongodb+srv://admin:admin1234@cluster0-vizhy.mongodb.net/test?retryWrites=true&w=majority"
MongoClient.connect(uri, function(err, client) {
   if(err) {
        console.log('Error occurred while connecting to MongoDB Atlas...\n',err);
   }
   console.log('Connected...');
   const collection = client.db("test").collection("devices");
   // perform actions on the collection object
   client.close();
});

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));
app.use('/api',student);
app.use('/api',teacher);

app.get('/api/get',async(req,res)=>{
    res.json({message:'RUNNING'});
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Listening on port ${port}...`));