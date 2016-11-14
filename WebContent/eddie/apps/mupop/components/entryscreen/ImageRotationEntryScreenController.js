var ImageRotationEntryScreenController = function(options) {}; // needed for detection

ImageRotationEntryScreenController.update = function(vars, data) {
	
};

//using http://kthornbloom.github.io/Smoothslides/
!function(a){a.fn.extend({smoothSlides:function(b){function i(){document.all&&!window.atob?a("#"+e).find(".ss-slide:last").animate({opacity:"0"}):a("#"+e).find(".ss-slide:last").css({transition:"all "+b.transitionDuration+"ms",opacity:"0"})}function j(){var b=a("#"+e).find(".ss-slide:eq(-2) img").prop("alt");b?a("#"+e).find(".ss-caption").css("opacity","1").html(b):a("#"+e).find(".ss-caption").css("opacity","0")}function k(){var b=a("#"+e).find(".ss-slide:eq(-1) img").prop("alt");b?a("#"+e).find(".ss-caption").css("opacity","1").html(b):a("#"+e).find(".ss-caption").css("opacity","0")}function m(){var b=a(d).find(".ss-paginate a").length,c=a(d).find("a.ss-paginate-current").index(),e=c+1;e>=b?(a(d).find("a.ss-paginate-current").removeClass(),a(d).find(".ss-paginate a:eq(0)").addClass("ss-paginate-current")):(a(d).find("a.ss-paginate-current").removeClass(),a(d).find(".ss-paginate a:eq("+e+")").addClass("ss-paginate-current"))}function n(){var b=a(d).find(".ss-paginate a").length,c=a(d).find("a.ss-paginate-current").index(),e=c-1;-2>=e?(a(d).find("a.ss-paginate-current").removeClass(),a(d).find(".ss-paginate a:eq("+b+")").addClass("ss-paginate-current")):(a(d).find("a.ss-paginate-current").removeClass(),a(d).find(".ss-paginate a:eq("+e+")").addClass("ss-paginate-current"))}var c={effectDuration:5e3,transitionDuration:500,effectModifier:1.3,order:"normal",autoPlay:"true",effect:"zoomOut,zoomIn,panUp,panDown,panLeft,panRight,diagTopLeftToBottomRight,diagTopRightToBottomLeft,diagBottomRightToTopLeft,diagBottomLeftToTopRight",effectEasing:"ease-in-out",nextText:" \u25ba",prevText:"\u25c4 ",captions:"true",navigation:"true",pagination:"true",matchImageSize:"true"},b=a.extend(c,b),d=this,e=a(this).attr("id"),f=b.effectDuration+b.transitionDuration,g=a(this).find("img").width(),h=.25*(100*b.effectModifier-100);if(b.transitionDuration>=b.effectDuration&&console.log("Make sure effectDuration is greater than transitionDuration"),a("#"+e).removeClass("smoothslides").addClass("smoothslides-on"),d.crossFade=function(){i(),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale(1)  rotate(0deg)"})},b.transitionDuration)},d.zoomOut=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") rotate(1.5deg)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale(1)  rotate(0deg)"})},b.transitionDuration)},d.zoomIn=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale(1.1) rotate(-1.5deg)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") rotate(0deg)"})},b.transitionDuration)},d.panLeft=function(){a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateX("+h+"%)"}),i(),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateX(0%)"})},b.transitionDuration)},d.panRight=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateX(-"+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateX(0%)"})},b.transitionDuration)},d.panUp=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY("+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%)"})},b.transitionDuration)},d.panDown=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY(-"+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%)"})},b.transitionDuration)},d.diagTopLeftToBottomRight=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY(-"+h+"%) translateX(-"+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%) translateX(0%)"})},b.transitionDuration)},d.diagBottomRightToTopLeft=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY("+h+"%) translateX("+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%) translateX(0%)"})},b.transitionDuration)},d.diagTopRightToBottomLeft=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY(-"+h+"%) translateX("+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%) translateX(0%)"})},b.transitionDuration)},d.diagBottomLeftToTopRight=function(){i(),a(this).find(".ss-slide:eq(-2)").css({transition:"none",transform:"scale("+b.effectModifier+") translateY("+h+"%) translateX(-"+h+"%)"}),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale("+b.effectModifier+") translateY(0%) translateX(0%)"})},b.transitionDuration)},"true"==b.matchImageSize?(a("#"+e).css("maxWidth",g),a("#"+e+" img").css("maxWidth","100%")):a("#"+e+" img").css("width","100%"),a(this).children().each(function(){a(this).wrap('<div class="ss-slide"></div>')}),a.fn.smoothslidesRandomize=function(b){return(b?this.find(b):this).parent().each(function(){a(this).children(b).sort(function(){return Math.random()-.5}).detach().appendTo(this)}),this},"random"==b.order?a("#"+e).smoothslidesRandomize(".ss-slide"):a("#"+e+" .ss-slide").each(function(){a(this).prependTo("#"+e)}),a("#"+e+" .ss-slide:first").css("position","relative"),"true"==b.autoPlay&&a(".ss-slide:first",this).appendTo(this),a(this).wrapInner("<div class='ss-slide-stage'></div>"),a(".ss-slide",this).each(function(){a(this).css({transition:"all "+b.effectDuration+"ms "+b.effectEasing})}),"true"==b.captions)if(a(d).append("<div class='ss-caption-wrap'><div class='ss-caption'></div></div>"),"true"==b.autoPlay)j();else{var l=a("#"+e).find(".ss-slide:last img").prop("alt");l?a("#"+e).find(".ss-caption").css("opacity","1").html(l):a("#"+e).find(".ss-caption").css("opacity","0")}"true"==b.navigation&&a(d).append('<a href="#" class="ss-prev ss-prev-on">'+b.prevText+'</a><a href="#" class="ss-next ss-next-on">'+b.nextText+"</a>"),"true"==b.pagination&&(a(d).append('<div class="ss-paginate-wrap"><div class="ss-paginate"></div></div>'),a(".ss-slide",d).each(function(){a(".ss-paginate",d).append('<a href="#"></a>')}),"true"==b.autoPlay?a(".ss-paginate a:last",d).addClass("ss-paginate-current"):a(".ss-paginate a:first",d).addClass("ss-paginate-current"));var o=function(){if(document.all&&!window.atob)d.crossFade();else if(a("#"+e).find(".ss-slide:eq(-2) img").attr("data-effect")){var c=a("#"+e).find(".ss-slide:eq(-2) img").attr("data-effect");d[c]()}else{effectArray=b.effect.split(",");var f=effectArray[Math.floor(Math.random()*effectArray.length)];d[f]()}j(),m()};if("true"==b.autoPlay){o();var p=setInterval(o,f)}a(".ss-prev, .ss-next, .ss-paginate",d).mouseover(function(){clearInterval(p)}).mouseout(function(){p=setInterval(o,f)}),a("#"+e).on("click",".ss-next-on",function(c){a(".ss-next-on",d).removeClass("ss-next-on"),a(d).find(".ss-slide:last").css({transition:"all "+b.transitionDuration+"ms",opacity:"0"}),j(),m(),setTimeout(function(){a(d).find(".ss-slide:last").prependTo(a(".ss-slide-stage",d)).css({opacity:"1",transform:"none"}),a(d).find(".ss-slide:last").css({transition:"all "+b.effectDuration+"ms "+b.effectEasing,transform:"scale(1)  rotate(0deg)"}),a(".ss-next",d).addClass("ss-next-on")},b.transitionDuration),c.preventDefault()}),a("#"+e).on("click",".ss-prev-on",function(c){a(".ss-prev-on",d).removeClass("ss-prev-on"),a("#"+e).find(".ss-slide:first").css({transition:"none",opacity:"0"}).appendTo("#"+e+" .ss-slide-stage"),a("#"+e).find(".ss-slide:last").css("opacity"),a("#"+e).find(".ss-slide:last").css({transition:"all "+b.transitionDuration+"ms",opacity:"1"}),k(),n(),setTimeout(function(){a(".ss-prev").addClass("ss-prev-on")},b.transitionDuration),c.preventDefault()}),a("#"+e).on("click",".ss-prev, .ss-next",function(a){a.preventDefault()}),a("#"+e).on("click",".ss-paginate a",function(b){var c=a(this).index(),d=a("#"+e+" .ss-paginate-current").index();if(d>c)for(var f=d-c,g=0;f>g;g++)a("#"+e).find(".ss-slide:first").appendTo("#"+e+" .ss-slide-stage");else if(c>d)for(var f=c-d,g=0;f>g;g++)a("#"+e).find(".ss-slide:last").prependTo("#"+e+" .ss-slide-stage");a("#"+e).find(".ss-paginate-current").removeClass(),a("#"+e).find(".ss-paginate a:eq("+c+")").addClass("ss-paginate-current");var h=a("#"+e).find(".ss-slide:eq(-1) img").prop("alt");h?a("#"+e).find(".ss-caption").css("opacity","1").html(h):a("#"+e).find(".ss-caption").css("opacity","0"),b.preventDefault()})}})}(jQuery);

function initImageRotationEntryScreen() {	
	$('#images').smoothSlides({
		effectDuration: 10000,
		transitionDuration: 1500,
		order: 'random',
		effectModifier: 1.4,
		navigation: 'false',
		pagination: 'false',
		matchImageSize: 'false'	
	});
}

//use small timeout to make sure the above plugin is loaded properly
setTimeout(initImageRotationEntryScreen, 200);