const express=require('express');
const router=express.Router();
const _ =require('lodash');
const {Faculty, validateUser, validateLogin} = require('../models/teacher');
const bcrypt=require('bcrypt');

router.get('/getf', async (req,res) =>{
    const faculty =await Faculty.findOne();
    if(faculty) return res.json({status:'1', message:'success' , isClass: faculty.isClass, classes:faculty.classes, dates:faculty.dates });
});

router.put('/isclass', async (req,res)=>{
    const faculty = await Faculty.findOne();
    faculty.isClass = req.body.isClass;
    if(faculty.isClass==false)
    {
        await faculty.save();
        return res.json({status:'1',message:'Success'});
    }
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); 
    var yyyy = today.getFullYear();

    today = dd + '/' + mm + '/' + yyyy;

    if(faculty.dates.length >=0)
    {
        var last = faculty.dates[faculty.dates.length - 1];
        if(today==last){
            return res.json({status:'0',message:'Multiple classes cannot be held on a single day'});
        }
    }

    await faculty.save();
    res.json({status:'1',message:'Success'});
});

router.put('/addClass', async (req,res)=>{
    const faculty=await Faculty.findOne();
    faculty.classes += parseInt(req.body.addClass);

    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); 
    var yyyy = today.getFullYear();

    today = dd + '/' + mm + '/' + yyyy;

    if(faculty.dates.length >=0)
    {
        var last = faculty.dates[faculty.dates.length - 1];
        if(today==last){
            return res.json({status:'0',message:'Multiple classes cannot be held on a single day'});
        }
    }
    
    faculty.dates.push(today);

    await faculty.save();
    res.json({status:'1',message:'Success'});
});

router.post('/faculty', async (req,res) => {
    const { error } = validateUser(req.body); 
    if (error) return res.json({status:'0', message: error.details[0].message});

    let faculty = await Faculty.findOne({ email: req.body.email });
    if (faculty) return res.json({status:'0',message:'User already registered.'});

    faculty = new Faculty(_.pick(req.body, ['name', 'email', 'password']));
    const salt = await bcrypt.genSalt(10);
    faculty.password = await bcrypt.hash(faculty.password, salt);
    await faculty.save();

    res.json({status:'1',message:'Registeration Success'});
});

router.post('/loginf', async (req,res) =>{
    const {error} = validateLogin(req.body);
    if (error) return res.json({status:'0', message: error.details[0].message});

    let faculty = await Faculty.findOne({ email: req.body.email });
    if (!faculty) return res.json({status:'0',message:'Invalid email or password.'});

    const validPassword = await bcrypt.compare(req.body.password, faculty.password);
    if (!validPassword) return res.json({status:0,message:'Invalid email or password.'})

    res.json({status:'1', message:'Login Success', name: faculty.name, email: faculty.email});
});

module.exports=router;

