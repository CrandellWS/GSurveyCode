// CopyRight (c) 2013 John Robinson - http://www.storminthecastle.com

var Goo = function(o) {

  var self = this;
  
    // Setup defaults
  self.type = "2d";
  self.animate = true;
  self.fullscreen = false;
  self.keysDown = {};
  self.userData = {};

  if (o) {
   for (var p in o) {
     if (!o.hasOwnProperty(p)) continue;
     self[p] = o[p];
   }
  }
  
  self.canvas = document.createElement("canvas");
  if (self.canvas)
    self.ctx = self.canvas.getContext(self.type);

  if (!self.canvas || !self.ctx && self.onFailure) {
    self.onFailure();
    return;
  }
  
  self.canvas.width = self.width;
  self.canvas.height = self.height;

  if (self.__defineGetter__ && self.__defineSetter__) { 
    self.__defineGetter__("width", function() {
      return self.canvas.width;
    });       
    self.__defineSetter__("width", function(v) {
      self.canvas.width = v;
    });

    self.__defineGetter__("height", function() {    
      return self.canvas.height;
    });       
    self.__defineSetter__("height", function(v) {
      self.canvas.height = v;
    });
  }
  else if (Object.defineProperty) {  // Try the IE way
    Object.defineProperty(self, "width", {
      get: function() {
        return self.canvas.width;
      },
      set: function(v) {
        self.canvas.width = v;
      }
    });
    Object.defineProperty(self, "height", {
      get: function() {
        return self.canvas.height;
      },
      set: function(v) {
        self.canvas.height = v;
      }
    });
  }

  if (self.fullscreen) {
    self.container = document.body;
    document.body.style.margin = '0px';
    document.body.style.padding = '0px';
    document.body.style.overflow = 'hidden';            
  }
  
  if (self.container) {
    self.container.appendChild(self.canvas);
  }
  
  // shim layer with setTimeout fallback
  var requestAnimFrame = (function () {
    var rAF = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame;
    if (!rAF) {
      rAF = function (callback) {
        window.setTimeout(callback, 1000 / 30.0);
      };
    }
    return rAF;
  })();

  var getTick = Date.now?Date.now: function () {
      return new Date().getTime();
    }

  self.updateMouse = function(event) {
    var self = this;
    var mousePos =  self.canvas.relMouseCoords(event);
    self.prevMouseX = self.mouseX;
    self.prevMouseY = self.mouseY;
    self.mouseX = mousePos.x;
    self.mouseY = mousePos.y;
  }

  function relMouseCoords(event){
    var totalOffsetX = 0;
    var totalOffsetY = 0;
    var canvasX = 0;
    var canvasY = 0;
    var currentElement = this;

    do{
      totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
      totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
    }
    while(currentElement = currentElement.offsetParent)

    canvasX = event.pageX - totalOffsetX;
    canvasY = event.pageY - totalOffsetY;

    return {x:canvasX, y:canvasY}
  }
  HTMLCanvasElement.prototype.relMouseCoords = relMouseCoords;

  document.addEventListener("mousedown", function(e) {
      if (e.target == self.canvas) {
        self.updateMouse(e);
        if (self.onMouseDown)
          self.onMouseDown(self);
        self.dragging = true;
      }
    }, false);
    
  document.addEventListener("mouseup", function(e) {
      if (self.dragging) {
      self.updateMouse(e);
      if (self.onMouseUp)
        self.onMouseUp(self);
      self.dragging = false;
      }
    }, false);
    
  document.addEventListener("mousemove", function(e) {
      self.updateMouse(e);
      if (self.mouseX != self.prevMouseX || self.mouseY != self.prevMouseY) {
        if (self.dragging && self.onMouseDrag)
          self.onMouseDrag(self);
        else if (self.onMouseMove && e.target == self.canvas)
          self.onMouseMove(self);
      }
    }, false);


  document.addEventListener("touchstart", function(e) {
      if (e.target == self.canvas) {
        self.updateMouse(e);
        if (self.onMouseDown)
          self.onMouseDown(self);
        self.dragging = true;
      }
    }, false);
    
  document.addEventListener("touchend", function(e) {
      if (self.onMouseUp)
        self.onMouseUp(self);
      self.dragging = false;
    }, false);
          
  document.addEventListener("touchmove", function(e) {
      if (self.dragging)
      {
        self.updateMouse(e);
        self.onMouseDrag(self);
        e.preventDefault();
      }
      /* Doesn't really make sense
      else if (self.onMouseMove && e.target == self.canvas)
        self.onMouseMove(self);
      */
  }, false);
    
  document.addEventListener("click", function(e) {
      self.updateMouse(e);
      if (self.onMouseClick)
        self.onMouseClick(self);
    }, false);
          
  document.addEventListener("keydown", function(e) {
      self.keyCode = e.keyCode;
      self.key =  String.fromCharCode(self.keyCode);
      self.keysDown[self.keyCode] = true;
      if (self.onKeyDown) self.onKeyDown(self)
    }, false);
    
  document.addEventListener("keyup", function(e) {
      self.keyCode = e.keyCode;
      self.key =  String.fromCharCode(self.keyCode);
      delete self.keysDown[self.keyCode];
      if (self.onKeyUp) self.onKeyUp(self)
    }, false);

  document.addEventListener("keypress", function(e) {
      self.keyCode = e.keyCode;
      self.key =  String.fromCharCode(self.keyCode);
      if (self.onKeyPress) self.onKeyPress(self)
    }, false);
      
   var sizeCanvas = (function() {
    if (self.fullscreen) {
      // This performs better than listening for resize events
      var w = window.innerWidth;
      var h = window.innerHeight;
      if (self.canvas.width != w)
        self.canvas.width = w;
      if (self.canvas.height != h)
        self.canvas.height = h;
    }  
  });
  
  var fpsCounter = 0;
  var fpsStartTime = getTick();
  
  function update() {
    sizeCanvas();
    var tick = getTick();
    if (self.onDraw) 
      self.onDraw(self, tick);
    if (self.animate) {
      if (fpsCounter++ > 60) {
        self.fps = fpsCounter / (tick-fpsStartTime) * 1000;
        if (self.onFrameRate)
          self.onFrameRate(self);
        fpsCounter = 0;
        fpsStartTime = tick;
      }
      requestAnimFrame(update);
    }
  };
  requestAnimFrame(update);
};
