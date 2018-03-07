//Node Modules
var http = require('http');
const bodyParser = require("body-parser"); 
var cookieParser = require("cookie-parser");
var session = require("express-session");
var flash = require("connect-flash");
var socketio = require('socket.io');
var express = require('express');

var router = express();
var server = http.createServer(router);
var io = socketio.listen(server);

//Custom Node Modules
var clientRoute = require("./node_require/client");

console.log('Run');

const path = require("path");

router.use(express.static(path.join(__dirname,'/client')));
router.use(bodyParser.urlencoded({ extended: true }));
router.use(bodyParser.json());
router.use(cookieParser());
router.use(session({secret : 'text',resave: false,saveUninitialized: true}));
router.use(flash());

io.on('connection', function (socket) {
    console.log("Connected");
    
    
    socket.on('logged', function (msg) {
        console.log(msg);
        socket.broadcast.emit('logged', msg);
    });
    
    socket.on('order', function (msg) {
        console.log(msg);
        socket.broadcast.emit('order', msg);
        
    });
    
});

router.set('views',path.join(__dirname,'/client/'));
router.set('view engine','ejs');

//Routes
router.use('/',clientRoute);

server.listen(process.env.PORT || 3000, process.env.IP || "127.0.0.1", function(){
  var addr = server.address();
  console.log("Chat server listening at", addr.address + ":" + addr.port);
});