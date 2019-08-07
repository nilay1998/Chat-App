const Joi = require('joi');
const mongoose = require('mongoose');

const menuSchema = new mongoose.Schema({
  name: String,
  email: String,
  password: String,
  classes: {type:Number, default:0},
  isClass: {type:Boolean, default:false},
  dates:{
    type:Array,
    default: []
  }
});


const Faculty = mongoose.model('Faculty', menuSchema);

function validateLogin(req) {
    const schema = {
      email: Joi.string().min(5).max(255).required().email(),
      password: Joi.string().min(5).max(255).required()
    };
    return Joi.validate(req, schema);
}

function validateUser(user) {
    const schema = {
      name: Joi.string().min(5).max(50).required(),
      email: Joi.string().min(5).max(255).required().email(),
      password: Joi.string().min(5).max(255).required()
    };
    return Joi.validate(user, schema);
}

exports.Faculty = Faculty; 
exports.validateUser = validateUser;
exports.validateLogin = validateLogin;