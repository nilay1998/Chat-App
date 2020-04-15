const express=require('express');
const router=express.Router();
const _ =require('lodash');
const {Student, validateID, validateUser, validateLogin} = require('../models/student');
const {Faculty} = require('../models/teacher');
const bcrypt=require('bcrypt');

router.get('/getall', async (req,res) =>{
    const student =await Student.find();
    if(student) return res.json({status:'1', message:'success' ,allItems: student });
});

router.get('/get/:email', async (req,res) =>{
    const student =await Student.findOne({ email: req.params.email });
    if(student) return res.json({status:'1', message:'success' ,attendance: student.attendance });
});

router.post('/login', async (req,res) =>{
    const {error} = validateLogin(req.body);
    if (error) return res.json({status:'0', message: error.details[0].message});

    let user = await Student.findOne({ email: req.body.email });
    if (!user) return res.json({status:'0',message:'Invalid email or password.'});

    const validPassword = await bcrypt.compare(req.body.password, user.password);
    if (!validPassword) return res.json({status:0,message:'Invalid email or password.'})

    res.json({status:'1', message:'Login Success', name:user.name, email:user.email, rollNumber:user.rollNumber });
});

router.put('/user', async(req,res) =>{
    const { error } = validateUser(req.body); 
    if (error) return res.json({status:'0', message: error.details[0].message});

    let student = await Student.findOne({ email: req.body.email });
    if (student) return res.json({status:'0',message:'Student already registered.'});

    student = await Student.findOne({ id:req.body.id});
    if(!student || student.email) return res.json({status:'0', message:'Wrong Id entered'});

    const salt = await bcrypt.genSalt(10);
    req.body.password = await bcrypt.hash(req.body.password, salt);

    student = await Student.findOneAndUpdate({id:req.body.id},_.pick(req.body, ['name', 'email', 'password','rollNumber']));
    
    res.json({status:'1',message:'Registeration Success'});
});

router.put('/attendance', async(req,res) =>{
    const { error } = validateID(req.body); 
    if (error) return res.json({status:'0', message: error.details[0].message});

    let student = await Student.findOne({id:req.body.id});
    if(!student) return res.json({status:'0', message:'Wrong Id entered'});

    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); 
    var yyyy = today.getFullYear();

    today = dd + '/' + mm + '/' + yyyy;

    const faculty=await Faculty.findOne();
    if(faculty.isClass==false)
    {
        return res.json({status:'0',message:'Caught cheating'});
    }

    if(student.attendance.length >=0)
    {
        var last = student.attendance[student.attendance.length - 1];
        if(today==last){
            return res.json({status:'0',message:'Multiple attendance cannot be marked on a single day'});
        }
    }

    student.attendance.push(today);
    await student.save();

    res.json({status:'1',message:'Attendance Updated'});
});


router.post('/id', async (req,res) => {
    const { error } = validateID(req.body); 
    if (error) return res.json({status:'0', message: error.details[0].message});

    const user = new Student(_.pick(req.body, ['id']));
    await user.save();

    res.json({status:'1',message:'Registeration Success'});
});

module.exports=router;