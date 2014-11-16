var Canvas = require('canvas');
var Image = Canvas.Image;
var qrcode = require('jsqrcode')(Canvas);
var util = require("util");
var events = require("events");

var QR = function (images) {
  events.EventEmitter.call(this);
  this.images = images;
};

util.inherits(QR, events.EventEmitter);

QR.prototype.start = function () {
  var self = this;
  var queuedImage = null;

  this.images.on('data', queueImageForProcessingIfIdle);

  function queueImageForProcessingIfIdle(image) {
    // only queue an image for scanning if there isn't one waiting
    // in order to avoid building up lag if we can't keep up
    if (queuedImage === null) {
      queuedImage = image;

      // scan the image *after* any more images arrive
      setTimeout(scanForCode, 0);
    }
  }

  function scanForCode() {
    try {
      var image = new Image;
      image.src = queuedImage;
      self.emit('qrcode', qrcode.decode(image));
    } catch (e) {
      // probably didn't find a code
    }
    queuedImage = null;
  }
};

module.exports = QR;
