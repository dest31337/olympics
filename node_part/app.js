var config = require('./config.js')
  , express = require('express')
  , Drone = require('./drone')
  , http = require('http')
  , path = require('path');

var drone = new Drone();

var app = express();

global.fakeVideo = false;

// all environments
app.set('port', config.SERVER_PORT);
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/drone/battery?', function(req, res){
    res.send(drone.getBattery().toString());
});

app.get('/drone/navdata?', function(req, res){
    res.send(drone.getNavdata());
});

app.get('/drone/qr?', function(req, res){
    res.send(drone.getQR());
});

app.get("/drone/image?", function(req, res) {
    res.writeHead(200, {
        "Content-Type": "image/png"
    });
    return res.end(drone.getImage(), "binary");
});

app.get('/drone/:command?', function(req, res){
    drone.runCommand(req.params.command, req.query);
    res.send("ok");
});

var server = http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});

// create h.264 video stream
require("dronestream").listen(server, {ip: config.DRONE_IP});