const express = require('express');
const app = express();
const bodyParser=require('body-parser');
const student=require('./routes/student');
const teacher=require('./routes/teacher');
const mongoose=require('mongoose');

require('./prod')(app);

mongoose.connect('mongodb+srv://nilaygupta1998:Nyga1998%21@cluster0-jok8l.mongodb.net/test?retryWrites=true&w=majority')
  .then(() => console.log('Connected to MongoDB...'))
  .catch(err => console.error('Could not connect to MongoDB...'));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));
app.use('/api',student);
app.use('/api',teacher);

app.get('/api/get',async(req,res)=>{
    res.json({message:'RUNNING'});
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Listening on port ${port}...`));