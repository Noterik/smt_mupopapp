(function(context){
	//Returns a basic bounding rect object instead of DOMRect, easier to compare. 
	function getBoundingClientRect(element) {
	  const rect = element.getBoundingClientRect();
	  return {
	    top: rect.top,
	    right: rect.right,
	    bottom: rect.bottom,
	    left: rect.left,
	    width: rect.width,
	    height: rect.height,
	    x: rect.x,
	    y: rect.y
	  };
	}
	
	//Checks if two objects are equal (only one level deep)
	function areEqualShallow(a, b) {
	    for(var key in a) {
	        if(!(key in b) || a[key] !== b[key]) {
	            return false;
	        }
	    }
	    for(var key in b) {
	        if(!(key in a) || a[key] !== b[key]) {
	            return false;
	        }
	    }
	    return true;
	}

	
	//Checks whether an element has a class
	function hasClass(el, className) {
	  if (el.classList)
	    return el.classList.contains(className)
	  else
	    return !!el.className.match(new RegExp('(\\s|^)' + className + '(\\s|$)'))
	}

	//Adds a class to an element if it doesn't already have it.
	function addClass(el, className) {
	  if (el.classList)
	    el.classList.add(className)
	  else if (!hasClass(el, className)) el.className += " " + className
	}
	
	//Removes a class from an element if it has it. 
	function removeClass(el, className) {
	  if (el.classList)
	    el.classList.remove(className)
	  else if (hasClass(el, className)) {
	    var reg = new RegExp('(\\s|^)' + className + '(\\s|$)')
	    el.className=el.className.replace(reg, ' ')
	  }
	}
	
	//Removes or adds a class dependening on whether the element already has it, if it doesn't have it, adds it, if has it, removes it. 
	function toggleClass(el, className) {
	  if(hasClass(el, className)) {
	    removeClass(el, className);
	  } else {
	    addClass(el, className);
	  }
	}
	
	//Sets the style on an element, only updates the style object if the key of the style is not set, or if its value is different. 
	function setStyle(el, newStyle){
	  for(var key in newStyle){
	    var val = newStyle[key];
	    
	    if(el.style[key] !== val) {
	      el.style[key] = val;
	    }
	  }
	}
	
	//Sets an attribute on an element, only updates or adds the attribute if it doesn't exist or its value is different. 
	function setAttributes(el, attributes){
	  for(var key in attributes){
	    var val = attributes[key];
	    
	    if(el[key] !== val){
	      el[key] = val;
	    }
	  }
	}
	
	//The Glass that is visible over the Zoomable image. Has an update function that takes new properties and calls render(). 
	function Glass(parent, props) {
	  var el = document.createElement('div');
	  el.className = 'ntk--magnify-glass';  
	  
	  var background = document.createElement('img');
	  background.className = 'ntk--glass-image'
	  background.src = props.img;
	  
	  el.appendChild(background);
	  parent.appendChild(el);
	  
	  var createCSSTransform = function(){
		 
		var transform;
		if(this.props.position.x && this.props.position.y && this.props.rate && this.props.radius) {
			var radius = this.props.radius / this.props.rate;
	    	var x = -(this.props.position.x - radius / 2);
	    	var y = -(this.props.position.y - radius / 2); 
	      	transform = `translate(${(x)}px, ${(y)}px)`;
	    }
		
	    var rate = this.props.rate;
	    //transform = `${transform} scale(${rate})`;
	    
	    return transform;
	  }.bind(this);

	  
	  this.render = function(prevProps) {
		//Checks if visible prop has changed, if so toggles the visibility
	    if(!prevProps || this.props.visible !== prevProps.visible){
	      toggleClass(el, '__visible');
	    }
	    
	   
	    //Does a shallow equal check on the image rect, if it has changed, render the changes. 
	    if(
	      !prevProps || 
	      !areEqualShallow(this.props.imgRect, prevProps.imgRect)
	    ) { 
	      setStyle(background, {
	        width: `${this.props.imgRect.width}px`,
	        height: `${this.props.imgRect.height}px`,
	        top: `${this.props.imgRect.top}px`,
	        left: `${this.props.imgRect.left}px`
	      })
	    }
	    
	    if(!prevProps || 
	      this.props.radius !== prevProps.radius &&
	      this.props.rate
	    ) {
	      var radius = this.props.radius / this.props.rate;
	      setStyle(el, {
	        width: `${radius}px`,
	        height: `${radius}px`,
	        transform: `scale(${this.props.rate})`,
	        marginLeft: `${-(radius / 2)}px`,
	        marginTop: `${-(radius / 2)}px`,
	      })
	    }
	    
	    if(!prevProps || 
	      !areEqualShallow(this.props.position, prevProps.position) ||
	      (this.props.rate !== prevProps.rate)
	    ) {      
	    
	      var transform = createCSSTransform();
	      setStyle(background, {
	        transform: transform,
	      });
	            
	      setStyle(el, {
	        left: `${this.props.position.x}px`,
	        top: `${this.props.position.y}px`,
	      })
	    }
	  }.bind(this);
	  
	  this.destroy = function(){
	    el.parentElement.removeChild(el);
	  }.bind(this);
	  
	  this.update(props);
	}

	Glass.prototype.defaultProps = {
	  position: {
		  x: null,
		  y: null,
	  },
	  radius: 160,
	  imgRect: {
		  top: null,
		  left: null,
		  width: null,
		  height: null,
	  },
	  visible: false,
	};

	Glass.prototype.update = function(newProps) {
	  var oldProps = this.props || null;
	  this.props = Object.assign({}, this.defaultProps, this.props, newProps);
	  this.render(oldProps);
	}

	function Zoomable(el, props, parent) {  
	  var boundingBox = getBoundingClientRect(el);
	  var glasses = {};
	  var overlay = document.createElement('div'); 
	  
	  var parent = parent || window.document.body;
	  parent.appendChild(overlay);
	  var onResize = function(e) {
	    boundingBox = el.getBoundingClientRect();
	    overlay.className = 'ntk--magnify-overlay';
	    overlay.style.top = boundingBox.y + 'px';
	    overlay.style.left = boundingBox.x + 'px';
	    overlay.style.height = boundingBox.height + 'px';
	    overlay.style.width = boundingBox.width + 'px';
	    
	    Object.keys(glasses).forEach(function(id){
	    	var glass = glasses[id];
	    	glass.update({
	    		imgRect: boundingBox, 
	    	});
	    });
	  }.bind(this);
	  
	  window.addEventListener('resize', onResize);
	  el.addEventListener('load', onResize);
	  onResize();
	  	  
	  this.create = function(glassId, zoomRate) {
	    if(glasses[glassId]) glasses[glassId].destroy();
	    glasses[glassId] = new Glass(parent, {
	      visible: true,
	      img: el.src,
	      imgRect: boundingBox,
	      rate: zoomRate || props.defaultZoomRate,
	    });
	  }.bind(this);
	  
	  this.setPosition = function(glassId, x, y) {
	    var glass = glasses[glassId];
	    if(!glass){
	      this.create(glassId);
	    }
	        
	    glasses[glassId].update({
	    	position: {
	    		x: boundingBox.x + x,
	    		y: boundingBox.y + y,
	    	}
	    });
	  }.bind(this);
	  
	  this.setZoom = function(glassId, rate) {
	    glasses[glassId].update({
	      rate: rate,
	    });
	  }.bind(this);
	  
	  this.refresh = function(){
		onResize();
	  }.bind(this);
	 
	  this.destroy = function(glassId) {
	    if(glasses[glassId]) { 
	      glasses[glassId].destroy();
	      delete glasses[glassId];
	    }
	  }.bind(this);
	  
	  this.addEventListener = function(event, callback){
	    overlay.addEventListener(event, callback.bind(this));
	  }
	  this.removeEventListener = function(event, callback){
	    overlay.removeEventListener.apply(event, callback.bind(this));
	  }
	}
	
	if(!context.ntk){
		context.ntk = {};
	}
	
	context.ntk.Zoomable = Zoomable;
})(window);