const express = require("express");
var router = express.Router();

router.get('/',function(req,res){
    res.render('client-home');
});

router.get('/Table2',function(req,res){
    res.render('table2');
});

router.get('/Table1',function(req,res){
    res.render('client-home');
});

module.exports = router;