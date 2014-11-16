/**
 * Created with JetBrains WebStorm.
 * User: Andrey
 * Date: 25.05.13
 * Time: 2:21
 * Класс управления дроном
 */
var config = require('./config.js')
  , arDrone = require("ar-drone")
  , util = require("util")
  , events = require("events")
  , QRAR = require('qrar')
  , fs = require('fs');

var Drone = function(){
    this.client = arDrone.createClient( {ip: config.DRONE_IP} );
    this.client.config('general:navdata_demo', 'TRUE');
    this._navdata = null;

    var self = this;

    this._battery = -1;
    this.client.on('batteryChange', function(battery) {
        self._battery = battery;
    });

    this.client.on('navdata', function(data) {
        self._navdata = data;
        self.emit('navdata', data);
    });

    this._QR = "null";
    var codes = new QRAR(this.client);

    codes.on('qrcode', function (code) {
        console.log(code);
        self._QR = code;
    });

    codes.start();

    fs.readFile('./drone.png', function (err, data) {
        if (err) throw err;
        self._currentImg = data;
    });

    this._currentImg = null;
    this.client.createPngStream().on("data", function(frame) {
        self._currentImg = frame;
    });
};

util.inherits(Drone, events.EventEmitter);

var _p = Drone.prototype;

_p.getBattery = function(){
    return this._battery;
};

_p.getNavdata = function(){
    return this._navdata;
};

_p.getQR = function(){
    return this._QR;
};

_p.getImage = function(){
    return this._currentImg;
};

_p.runCommand  = function(cmd, args){
    console.log('Executing command: "' + cmd + '". With arguments: ' + JSON.stringify(args));
    switch(cmd){
        case "takeoff":
        case "land":
        case "stop":
        case "disableEmergency":
            this.client[cmd].apply(this.client, []);
            break;

        case "up":
        case "down":
        case "front":
        case "back":
        case "left":
        case "right":
        case "clockwise":
        case "counterClockwise":
            this.client[cmd].apply(this.client, [args.speed]);
            break;

        case "config":
            this.client.config(args.key, args.value);
            break;

        case "animate":
            this.client.animate(args.animation, args.duration);
            break;

        case "animateLeds":
            this.client.animateLeds(args.animation, args.hz, args.duration);
            break;

        case "move":
            args.x >= 0 ? this.client.right(args.x) : this.client.left(-args.x);
            args.y >= 0 ? this.client.front(args.y) : this.client.back(-args.y);
            args.z >= 0 ? this.client.up(args.z) : this.client.down(-args.z);
            args.rotate >= 0 ? this.client.clockwise(args.rotate) : this.client.counterClockwise(-args.rotate);
            break;
        default:
            throw new Error("ERROR: Unknown command");
    }
};


module.exports = Drone;