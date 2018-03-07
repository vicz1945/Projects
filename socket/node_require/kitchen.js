
var express = require("express");
var router = express.Router();



router.get('/kitchen',function(req,res){
    res.render('kitchen-home');
});

module.exports = router;