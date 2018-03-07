
var express = require("express");
var router = express.Router();



router.get('/manager',function(req,res){
    res.render('manager-home');
});

module.exports = router;