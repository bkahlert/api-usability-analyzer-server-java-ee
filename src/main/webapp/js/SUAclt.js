/**
 * Date Format 1.2.3 (c) 2007-2009 Steven Levithan <stevenlevithan.com> MIT
 * license
 * 
 * Includes enhancements by Scott Trenda <scott.trenda.net> and Kris Kowal
 * <cixar.com/~kris.kowal/>
 * 
 * Accepts a date, a mask, or a date and a mask. Returns a formatted version of
 * the given date. The date defaults to the current date/time. The mask defaults
 * to dateFormat.masks.default.
 */
var dateFormat=function(){var token=/d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,timezone=/\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,timezoneClip=/[^-+\dA-Z]/g,pad=function(val,len){val=String(val);len=len||2;while(val.length<len)val="0"+val;return val};return function(date,mask,utc){var dF=dateFormat;if(arguments.length==1&&Object.prototype.toString.call(date)=="[object String]"&&!/\d/.test(date)){mask=date;date=undefined}date=date?new Date(date):new Date;if(isNaN(date))throw SyntaxError("invalid date");mask=String(dF.masks[mask]||mask||dF.masks["default"]);if(mask.slice(0,4)=="UTC:"){mask=mask.slice(4);utc=true}var _=utc?"getUTC":"get",d=date[_+"Date"](),D=date[_+"Day"](),m=date[_+"Month"](),y=date[_+"FullYear"](),H=date[_+"Hours"](),M=date[_+"Minutes"](),s=date[_+"Seconds"](),L=date[_+"Milliseconds"](),o=utc?0:date.getTimezoneOffset(),flags={d:d,dd:pad(d),ddd:dF.i18n.dayNames[D],dddd:dF.i18n.dayNames[D+7],m:m+1,mm:pad(m+1),mmm:dF.i18n.monthNames[m],mmmm:dF.i18n.monthNames[m+12],yy:String(y).slice(2),yyyy:y,h:H%12||12,hh:pad(H%12||12),H:H,HH:pad(H),M:M,MM:pad(M),s:s,ss:pad(s),l:pad(L,3),L:pad(L>99?Math.round(L/10):L),t:H<12?"a":"p",tt:H<12?"am":"pm",T:H<12?"A":"P",TT:H<12?"AM":"PM",Z:utc?"UTC":(String(date).match(timezone)||[""]).pop().replace(timezoneClip,""),o:(o>0?"-":"+")+pad(Math.floor(Math.abs(o)/60)*100+Math.abs(o)%60,4),S:["th","st","nd","rd"][d%10>3?0:(d%100-d%10!=10)*d%10]};return mask.replace(token,function($0){return $0 in flags?flags[$0]:$0.slice(1,$0.length-1)})}}();dateFormat.masks={"default":"ddd mmm dd yyyy HH:MM:ss",shortDate:"m/d/yy",mediumDate:"mmm d, yyyy",longDate:"mmmm d, yyyy",fullDate:"dddd, mmmm d, yyyy",shortTime:"h:MM TT",mediumTime:"h:MM:ss TT",longTime:"h:MM:ss TT Z",isoDate:"yyyy-mm-dd",isoTime:"HH:MM:ss",isoDateTime:"yyyy-mm-dd'T'HH:MM:ss",isoUtcDateTime:"UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"};dateFormat.i18n={dayNames:["Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],monthNames:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","January","February","March","April","May","June","July","August","September","October","November","December"]};Date.prototype.format=function(mask,utc){return dateFormat(this,mask,utc)};

/**
 * A simple querystring parser. Example usage: var q = $.parseQuery();
 * q.fooreturns "bar" if query contains "?foo=bar"; multiple values are added to
 * an array. Values are unescaped by default and plus signs replaced with
 * spaces, or an alternate processing function can be passed in the params
 * object . http://actingthemaggot.com/jquery
 * 
 * Copyright (c) 2008 Michael Manning (http://actingthemaggot.com) Dual licensed
 * under the MIT (MIT-LICENSE.txt) and GPL (GPL-LICENSE.txt) licenses.
 */
(function($) {
	$.parseQuery = function(qs,options) {
		var q = (typeof qs === 'string'?qs:window.location.search), o = {'f':function(v){return unescape(v).replace(/\+/g,' ');}}, options = (typeof qs === 'object' && typeof options === 'undefined')?qs:options, o = jQuery.extend({}, o, options), params = {};
		jQuery.each(q.match(/^\??(.*)$/)[1].split('&'),function(i,p){
			p = p.split('=');
			p[1] = o.f(p[1]);
			params[p[0]] = params[p[0]]?((params[p[0]] instanceof Array)?(params[p[0]].push(p[1]),params[p[0]]):[params[p[0]],p[1]]):p[1];
		});
		return params;
	}
})(jQuery);

/**
 * Copyright (c) 2011-2012 Björn Kahlert (http://bkahlert.com) Dual licensed
 * under the MIT (MIT-LICENSE.txt) and GPL (GPL-LICENSE.txt) licenses.
 * 
 * 1) Some page (example.com) includes
 * http(s)://dalak.imp.fu-berlin.de/SUAsrv/static/js/SUAclt.js (this script) 2)
 * The page finished loading 3) SUAclt.js checks if the browser's fingerprint
 * has changed. Due to the same origin policy (example.com !=
 * dalak.imp.fu-berlin.de) SUAclt.js can't access a locally saved old
 * fingerprint. Therefore... (inspired by
 * http://www.nczonline.net/blog/2010/09/07/learning-from-xauth-cross-domain-localstorage/
 * and
 * http://www.ibm.com/developerworks/web/library/wa-crossdomaincomm/index.html?ca=drs-#N1019B)
 * 1) SUAclt.js inserts an invisible iframe and points to
 * http(s)://dalak.imp.fu-berlin.de/SUAsrv/static/SUAsrv.html?example.com 2) In
 * the context of http(s)://dalak.imp.fu-berlin.de SUAsrv.html saves the current
 * fingerprint as the last known fingerprint in the local storage (fallback to
 * userData in IE6/7/8). 3) SUAsrv.html sets its window.name to "SUAsrv-[last
 * fingerprint]-[new fingerprint] 4) SUAsrv.html redirects back to example.com
 * (which is the query portion in 3.1) and the query ?noSUA - redirecting to the
 * calling web site makes window.name accessible by it - ?noSUA disables data
 * collection in order to avoid infinitive recursion 5) SUAclt.js receives the
 * load event and extracts the last and current fingerprint from the iframe's
 * window.name 4.a) If the two fingerprints differ, associateNewFingerprint is
 * called and the logging is activated with a small delay. (SUAsrv does not
 * correctly handle immediate log events after a fingerprint change.) 4.b) If
 * the fingerprint has not changed the logging is immediately activated. 6) The
 * frame is deleted to save resources.
 * 
 */
(function($) {
	// prefer https over http; use http only if port is specified
	var SUAsrvURL = (("${HOST}".indexOf(":") >= 0) ? "http://" : "https://") + "${HOST}${CONTEXT_PATH}";
	
	function associateNewFingerprint(lastFingerprint, currentFingerprint) {
		$.ajax({
			url: SUAsrvURL + "/rest/fingerprint/!" + lastFingerprint + "/associate/!" + currentFingerprint,
			type: "GET",
			dataType: "jsonp"
		});
	}
	
	function lpad(str, padString, length) {
	    while (str.length < length)
	        str = padString + str;
	    return str;
	}
	
	function getISO8601Date() {
		var now = new Date();
		var date = now.format("yyyy-mm-dd'T'HH:MM:ss");
		date += "." + lpad(now.getMilliseconds()+"", "0", 3);
		date += now.format("o");
		return date.substring(0, date.length-2) + ":" + date.substr(date.length-2);
	}
	
	function log(fingerprint, e) {
		var iso8601date = getISO8601Date();
		data = {
			url: window.location.href,
			dateTime: iso8601date,
			event: e,
			"bounds.x": $(window).scrollLeft(),
			"bounds.y": $(window).scrollTop(),
			"bounds.width": $(window).width(),
			"bounds.height": $(window).height()
		};
		if($.parseQuery().id) data["id"] = $.parseQuery().id; // remove when no more cached version of this js exists
		if($.parseQuery().SUAid) data["id"] = $.parseQuery().SUAid;
		$.ajax({
			url: SUAsrvURL + "/rest/doclog/!" + fingerprint,
			data: data,
			type: "GET",
			dataType: "jsonp",
			success: function(data, status) {
				if(!${productionMode}) {
					console.log("Successfully logged event: " + e);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				if(!${productionMode}) {
					console.log("Error logging event: " + e);
				}
				// TODO send notification
			}
		});
	}
	
	// for test purposes
	window["SUAtestLog"] = log; // allow manual log invocations
	window["SUAsrvURL"] = SUAsrvURL; // url of SUAsrv
	
	var loggingActivated = false;
	function activateLogging(fingerprint) {
		if(loggingActivated) return;
		loggingActivated = true;
		
		// don't use $(...).on if jQuery is too old
		var useJQueryOn = typeof($(document).on) === "function";
		
		if(typeof(window["SUAactivateLogging"]) === "function") {
			window["SUAactivateLogging"](fingerprint);
		}
		
		log(fingerprint, "ready");
		
		var token = $.parseQuery().token;
		if(token) log(fingerprint, "survey-" + token);

		// TODO improve reliability of anker-click events
		// maybe use mousedown?
		var linkHandler = function(e) {
			log(fingerprint, "link-" + this.href);
		};
		if(useJQueryOn) {
			$("a").on("click", linkHandler);
		} else {
			$("a").live("click", linkHandler);
		}
		
		var typedCache = {};
		var typedHandler = function(e) {
			var id = this.id ? this.id : (this.name ? this.name : "");
			var value = $(this).val();
			
			if(typedCache[id] && typedCache[id] == value) return;
			typedCache[id] = value;
			
			log(fingerprint, "typing-" + id + "-" + value);
		};
		if(useJQueryOn) {
			$("input").on("keyup", typedHandler);
			$("textarea").on("keyup", typedHandler);
		} else {
			$("input").live("keyup", typedHandler);
			$("textarea").live("keyup", typedHandler);
		}
		
		$(window).focus(function() {
			log(fingerprint, "focus")
	    });
		
		var scrollTimerID;
		$(window).scroll(function() {
			if(scrollTimerID) window.clearTimeout(scrollTimerID);
			scrollTimerID = window.setTimeout(function() { log(fingerprint, "scroll"); }, 2000);
		});
		
		var resizeTimerID;
		$(window).resize(function() {
			if(resizeTimerID) window.clearTimeout(resizeTimerID);
			resizeTimerID = window.setTimeout(function() { log(fingerprint, "resize"); }, 1500);
		});

	    $(window).blur(function() {
	    	log(fingerprint, "blur")
	    });
	    
	    var unloadLogSent = false;
	    $(window).bind('beforeunload', function() {
	    	if(!unloadLogSent) {
	    		log(fingerprint, "unload");
	    		unloadLogSent = true;
	    	}
	    });
	    
	    $(window).unload(function() {
	    	if(!unloadLogSent) {
	    		log(fingerprint, "unload");
	    		unloadLogSent = true;
	    	}
		});
	}

	$(document).ready(function() {
		if(window.location.search == "?noSUA") {
			return;
		}
		
		var iframe = $("<iframe/>");
		if(${productionMode}) {
			iframe.css({ position: "absolute", width: "1px", height: "1px", top: "-9999px", left: "-9999px" });
		} else {
			iframe.css({ position: "fixed", width: "500px", height: "500px", right: "0px", bottom: "0px", border: "5px solid #ff0000", background: "#fff", opacity: 0.2 });
			iframe.hover(function() { iframe.css({ opacity: 0.7 }); }, function() { iframe.css({ opacity: 0.2 }); });
		}
		var returnURI = window.location.protocol + "//" + window.location.host + "?noSUA";
		iframe.attr("id", "SUAsrv");
		iframe.attr("src", SUAsrvURL + "/static/SUAsrv.html?" + encodeURI(returnURI));
		iframe.load(function() {
			fingerprints = iframe[0].contentWindow.name.split("-");
			setTimeout(function() { $(iframe).remove(); }, 1000); // must stay in place to be successfully polled by Selenium
			if(fingerprints.length == 3) {
				if(fingerprints[0] == "SUAsrv" && fingerprints[1] != fingerprints[2]) {
					associateNewFingerprint(fingerprints[1], fingerprints[2]);
					window.setTimeout(function() {
						activateLogging(fingerprints[2]);
					}, 500);
				} else {
					activateLogging(fingerprints[2]);
				}
			}
		});
        $("body").append(iframe);
	});
})(jQuery);