/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */
var dateFormat=function(){var token=/d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,timezone=/\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,timezoneClip=/[^-+\dA-Z]/g,pad=function(val,len){val=String(val);len=len||2;while(val.length<len)val="0"+val;return val};return function(date,mask,utc){var dF=dateFormat;if(arguments.length==1&&Object.prototype.toString.call(date)=="[object String]"&&!/\d/.test(date)){mask=date;date=undefined}date=date?new Date(date):new Date;if(isNaN(date))throw SyntaxError("invalid date");mask=String(dF.masks[mask]||mask||dF.masks["default"]);if(mask.slice(0,4)=="UTC:"){mask=mask.slice(4);utc=true}var _=utc?"getUTC":"get",d=date[_+"Date"](),D=date[_+"Day"](),m=date[_+"Month"](),y=date[_+"FullYear"](),H=date[_+"Hours"](),M=date[_+"Minutes"](),s=date[_+"Seconds"](),L=date[_+"Milliseconds"](),o=utc?0:date.getTimezoneOffset(),flags={d:d,dd:pad(d),ddd:dF.i18n.dayNames[D],dddd:dF.i18n.dayNames[D+7],m:m+1,mm:pad(m+1),mmm:dF.i18n.monthNames[m],mmmm:dF.i18n.monthNames[m+12],yy:String(y).slice(2),yyyy:y,h:H%12||12,hh:pad(H%12||12),H:H,HH:pad(H),M:M,MM:pad(M),s:s,ss:pad(s),l:pad(L,3),L:pad(L>99?Math.round(L/10):L),t:H<12?"a":"p",tt:H<12?"am":"pm",T:H<12?"A":"P",TT:H<12?"AM":"PM",Z:utc?"UTC":(String(date).match(timezone)||[""]).pop().replace(timezoneClip,""),o:(o>0?"-":"+")+pad(Math.floor(Math.abs(o)/60)*100+Math.abs(o)%60,4),S:["th","st","nd","rd"][d%10>3?0:(d%100-d%10!=10)*d%10]};return mask.replace(token,function($0){return $0 in flags?flags[$0]:$0.slice(1,$0.length-1)})}}();dateFormat.masks={"default":"ddd mmm dd yyyy HH:MM:ss",shortDate:"m/d/yy",mediumDate:"mmm d, yyyy",longDate:"mmmm d, yyyy",fullDate:"dddd, mmmm d, yyyy",shortTime:"h:MM TT",mediumTime:"h:MM:ss TT",longTime:"h:MM:ss TT Z",isoDate:"yyyy-mm-dd",isoTime:"HH:MM:ss",isoDateTime:"yyyy-mm-dd'T'HH:MM:ss",isoUtcDateTime:"UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"};dateFormat.i18n={dayNames:["Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],monthNames:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","January","February","March","April","May","June","July","August","September","October","November","December"]};Date.prototype.format=function(mask,utc){return dateFormat(this,mask,utc)};

/*
 * Joseph Myer's md5() algorithm wrapped in a self-invoked function to prevent
 * global namespace polution, modified to hash unicode characters as UTF-8.
 *  
 * Copyright 1999-2010, Joseph Myers, Paul Johnston, Greg Holt, Will Bond <will@wbond.net>
 * http://www.myersdaily.org/joseph/javascript/md5-text.html
 * 
 * Released under the BSD license
 * http://www.opensource.org/licenses/bsd-license
 */
/*!
 * Joseph Myer's md5() algorithm wrapped in a self-invoked function to prevent
 * global namespace polution, modified to hash unicode characters as UTF-8.
 *  
 * Copyright 1999-2010, Joseph Myers, Paul Johnston, Greg Holt, Will Bond <will@wbond.net>
 * http://www.myersdaily.org/joseph/javascript/md5-text.html
 * http://pajhome.org.uk/crypt/md5
 * 
 * Released under the BSD license
 * http://www.opensource.org/licenses/bsd-license
 */
(function() {
	function md5cycle(x, k) {
		var a = x[0], b = x[1], c = x[2], d = x[3];

		a = ff(a, b, c, d, k[0], 7, -680876936);
		d = ff(d, a, b, c, k[1], 12, -389564586);
		c = ff(c, d, a, b, k[2], 17, 606105819);
		b = ff(b, c, d, a, k[3], 22, -1044525330);
		a = ff(a, b, c, d, k[4], 7, -176418897);
		d = ff(d, a, b, c, k[5], 12, 1200080426);
		c = ff(c, d, a, b, k[6], 17, -1473231341);
		b = ff(b, c, d, a, k[7], 22, -45705983);
		a = ff(a, b, c, d, k[8], 7, 1770035416);
		d = ff(d, a, b, c, k[9], 12, -1958414417);
		c = ff(c, d, a, b, k[10], 17, -42063);
		b = ff(b, c, d, a, k[11], 22, -1990404162);
		a = ff(a, b, c, d, k[12], 7, 1804603682);
		d = ff(d, a, b, c, k[13], 12, -40341101);
		c = ff(c, d, a, b, k[14], 17, -1502002290);
		b = ff(b, c, d, a, k[15], 22, 1236535329);

		a = gg(a, b, c, d, k[1], 5, -165796510);
		d = gg(d, a, b, c, k[6], 9, -1069501632);
		c = gg(c, d, a, b, k[11], 14, 643717713);
		b = gg(b, c, d, a, k[0], 20, -373897302);
		a = gg(a, b, c, d, k[5], 5, -701558691);
		d = gg(d, a, b, c, k[10], 9, 38016083);
		c = gg(c, d, a, b, k[15], 14, -660478335);
		b = gg(b, c, d, a, k[4], 20, -405537848);
		a = gg(a, b, c, d, k[9], 5, 568446438);
		d = gg(d, a, b, c, k[14], 9, -1019803690);
		c = gg(c, d, a, b, k[3], 14, -187363961);
		b = gg(b, c, d, a, k[8], 20, 1163531501);
		a = gg(a, b, c, d, k[13], 5, -1444681467);
		d = gg(d, a, b, c, k[2], 9, -51403784);
		c = gg(c, d, a, b, k[7], 14, 1735328473);
		b = gg(b, c, d, a, k[12], 20, -1926607734);

		a = hh(a, b, c, d, k[5], 4, -378558);
		d = hh(d, a, b, c, k[8], 11, -2022574463);
		c = hh(c, d, a, b, k[11], 16, 1839030562);
		b = hh(b, c, d, a, k[14], 23, -35309556);
		a = hh(a, b, c, d, k[1], 4, -1530992060);
		d = hh(d, a, b, c, k[4], 11, 1272893353);
		c = hh(c, d, a, b, k[7], 16, -155497632);
		b = hh(b, c, d, a, k[10], 23, -1094730640);
		a = hh(a, b, c, d, k[13], 4, 681279174);
		d = hh(d, a, b, c, k[0], 11, -358537222);
		c = hh(c, d, a, b, k[3], 16, -722521979);
		b = hh(b, c, d, a, k[6], 23, 76029189);
		a = hh(a, b, c, d, k[9], 4, -640364487);
		d = hh(d, a, b, c, k[12], 11, -421815835);
		c = hh(c, d, a, b, k[15], 16, 530742520);
		b = hh(b, c, d, a, k[2], 23, -995338651);

		a = ii(a, b, c, d, k[0], 6, -198630844);
		d = ii(d, a, b, c, k[7], 10, 1126891415);
		c = ii(c, d, a, b, k[14], 15, -1416354905);
		b = ii(b, c, d, a, k[5], 21, -57434055);
		a = ii(a, b, c, d, k[12], 6, 1700485571);
		d = ii(d, a, b, c, k[3], 10, -1894986606);
		c = ii(c, d, a, b, k[10], 15, -1051523);
		b = ii(b, c, d, a, k[1], 21, -2054922799);
		a = ii(a, b, c, d, k[8], 6, 1873313359);
		d = ii(d, a, b, c, k[15], 10, -30611744);
		c = ii(c, d, a, b, k[6], 15, -1560198380);
		b = ii(b, c, d, a, k[13], 21, 1309151649);
		a = ii(a, b, c, d, k[4], 6, -145523070);
		d = ii(d, a, b, c, k[11], 10, -1120210379);
		c = ii(c, d, a, b, k[2], 15, 718787259);
		b = ii(b, c, d, a, k[9], 21, -343485551);

		x[0] = add32(a, x[0]);
		x[1] = add32(b, x[1]);
		x[2] = add32(c, x[2]);
		x[3] = add32(d, x[3]);
	}

	function cmn(q, a, b, x, s, t) {
		a = add32(add32(a, q), add32(x, t));
		return add32((a << s) | (a >>> (32 - s)), b);
	}

	function ff(a, b, c, d, x, s, t) {
		return cmn((b & c) | ((~b) & d), a, b, x, s, t);
	}

	function gg(a, b, c, d, x, s, t) {
		return cmn((b & d) | (c & (~d)), a, b, x, s, t);
	}

	function hh(a, b, c, d, x, s, t) {
		return cmn(b ^ c ^ d, a, b, x, s, t);
	}

	function ii(a, b, c, d, x, s, t) {
		return cmn(c ^ (b | (~d)), a, b, x, s, t);
	}

	function md51(s) {
		// Converts the string to UTF-8 "bytes" when necessary
		if (/[\x80-\xFF]/.test(s)) {
			s = unescape(encodeURI(s));
		}
		txt = '';
		var n = s.length, state = [1732584193, -271733879, -1732584194, 271733878], i;
		for (i = 64; i <= s.length; i += 64) {
			md5cycle(state, md5blk(s.substring(i - 64, i)));
		}
		s = s.substring(i - 64);
		var tail = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
		for (i = 0; i < s.length; i++)
		tail[i >> 2] |= s.charCodeAt(i) << ((i % 4) << 3);
		tail[i >> 2] |= 0x80 << ((i % 4) << 3);
		if (i > 55) {
			md5cycle(state, tail);
			for (i = 0; i < 16; i++) tail[i] = 0;
		}
		tail[14] = n * 8;
		md5cycle(state, tail);
		return state;
	}

	function md5blk(s) { /* I figured global was faster.   */
		var md5blks = [], i; /* Andy King said do it this way. */
		for (i = 0; i < 64; i += 4) {
			md5blks[i >> 2] = s.charCodeAt(i) +
			                  (s.charCodeAt(i + 1) << 8) +
			                  (s.charCodeAt(i + 2) << 16) +
			                  (s.charCodeAt(i + 3) << 24);
		}
		return md5blks;
	}

	var hex_chr = '0123456789abcdef'.split('');

	function rhex(n) {
		var s = '', j = 0;
		for (; j < 4; j++)
		s += hex_chr[(n >> (j * 8 + 4)) & 0x0F] +
		     hex_chr[(n >> (j * 8)) & 0x0F];
		return s;
	}

	function hex(x) {
		for (var i = 0; i < x.length; i++)
		x[i] = rhex(x[i]);
		return x.join('');
	}

	md5 = function (s) {
		return hex(md51(s));
	}

	/* this function is much faster, so if possible we use it. Some IEs are the
	only ones I know of that need the idiotic second function, generated by an
	if clause.  */
	function add32(a, b) {
		return (a + b) & 0xFFFFFFFF;
	}

	if (md5('hello') != '5d41402abc4b2a76b9719d911017c592') {
		function add32(x, y) {
			var lsw = (x & 0xFFFF) + (y & 0xFFFF),
			    msw = (x >> 16) + (y >> 16) + (lsw >> 16);
			return (msw << 16) | (lsw & 0xFFFF);
		}
	}
})();

// Browser fingerprinting is a technique to "mark" anonymous users using JS
// (or other things).  To build an "identity" of sorts the browser is queried
// for a list of its plugins, the screen size and several other things, then
// hashes them.  The idea is that these bits of information produce an unique
// "fingerprint" of sorts; the more elaborate the list of data points is, the
// more unique this fingerprint becomes.  And you wouldn't even need to set a
// cookie to recognize this user when she visits again.
// 
// For more information on this topic consult [Ars Technica](http://arstechnica.com/tech-policy/news/2010/05/how-your-web-browser-rats-you-out-online.ars)
// or the [EFF](http://panopticlick.eff.org/).  There is a lot of potential
// for undesirable shenanigans, and I strictly oppose using this technique for
// marketing and ad-related tracking purposes.
//
// Anyways, I needed a really simple fingerprinting library, so I wrote a
// quick and dirty jQuery plugin.  This is by no means a complete and
// watertight implementation -- it is merely the scratch for a particular itch
// I was having.  YMMV.
//
// This library was written by Carlo Zottmann, carlo@municode.de, has its home
// @ [Github](http://github.com/carlo/jquery-browser-fingerprint) and is
// WTF-licensed (see LICENSE.txt).

// Let's not bleed into the global namespace here...
( function($) {

  // Calling `jQuery.fingerprint()` will return an MD5 hash, i.e. said
  // fingerprint.
  $.fingerprint = function() {
  
    // This function, `_raw()`, uses several browser details which are 
    // available to JS here to build a string, namely...
    //
    // * the user agent
    // * screen size
    // * color depth
    // * the timezone offset
    // * the list of all installed plugins (we're using their names,
    //    descriptions, mime types and file name extensions here)
    function _raw() {
      // That string is the return value.
      return [
        navigator.userAgent,
        [ screen.height, screen.width, screen.colorDepth ].join("x"),
        ( new Date() ).getTimezoneOffset(),
        $.map( navigator.plugins, function(p) {
          return [
            p.name,p.description,
            $.map( p, function(mt) { return [ mt.type, mt.suffixes ].join("~"); }).join(",")
          ].join("::");
        }).join(";")
      ].join("###");
    }

    // `_md5()` computes a MD5 hash using [md5-js](http://github.com/wbond/md5-js/).
    function _md5() {
      if ( typeof window.md5 === "function" ) {
        // The return value is the hashed fingerprint string.
        return md5( _raw() );
      }
      else {
        // If `window.md5()` isn't available, an error is thrown.
        throw "md5 unavailable, please get it from http://github.com/wbond/md5-js/";
      }
    }
return "test2";
    // And, since I'm lazy, calling `$.fingerprint()` will return the hash
    // right away, without the need for any other calls.
    return _md5();
  }
  
})(jQuery);

/**
 * A simple querystring parser.
 * Example usage: var q = $.parseQuery(); q.fooreturns  "bar" if query contains "?foo=bar"; multiple values are added to an array. 
 * Values are unescaped by default and plus signs replaced with spaces, or an alternate processing function can be passed in the params object .
 * http://actingthemaggot.com/jquery
 *
 * Copyright (c) 2008 Michael Manning (http://actingthemaggot.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 **/
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
 * http://code.google.com/p/jquery-json/
 * Required by JStorage
 */
(function($){var escapeable=/["\\\x00-\x1f\x7f-\x9f]/g,meta={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'};$.toJSON=typeof JSON==='object'&&JSON.stringify?JSON.stringify:function(o){if(o===null){return'null';}
var type=typeof o;if(type==='undefined'){return undefined;}
if(type==='number'||type==='boolean'){return''+o;}
if(type==='string'){return $.quoteString(o);}
if(type==='object'){if(typeof o.toJSON==='function'){return $.toJSON(o.toJSON());}
if(o.constructor===Date){var month=o.getUTCMonth()+1,day=o.getUTCDate(),year=o.getUTCFullYear(),hours=o.getUTCHours(),minutes=o.getUTCMinutes(),seconds=o.getUTCSeconds(),milli=o.getUTCMilliseconds();if(month<10){month='0'+month;}
if(day<10){day='0'+day;}
if(hours<10){hours='0'+hours;}
if(minutes<10){minutes='0'+minutes;}
if(seconds<10){seconds='0'+seconds;}
if(milli<100){milli='0'+milli;}
if(milli<10){milli='0'+milli;}
return'"'+year+'-'+month+'-'+day+'T'+
hours+':'+minutes+':'+seconds+'.'+milli+'Z"';}
if(o.constructor===Array){var ret=[];for(var i=0;i<o.length;i++){ret.push($.toJSON(o[i])||'null');}
return'['+ret.join(',')+']';}
var name,val,pairs=[];for(var k in o){type=typeof k;if(type==='number'){name='"'+k+'"';}else if(type==='string'){name=$.quoteString(k);}else{continue;}
type=typeof o[k];if(type==='function'||type==='undefined'){continue;}
val=$.toJSON(o[k]);pairs.push(name+':'+val);}
return'{'+pairs.join(',')+'}';}};$.evalJSON=typeof JSON==='object'&&JSON.parse?JSON.parse:function(src){return eval('('+src+')');};$.secureEvalJSON=typeof JSON==='object'&&JSON.parse?JSON.parse:function(src){var filtered=src.replace(/\\["\\\/bfnrtu]/g,'@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,']').replace(/(?:^|:|,)(?:\s*\[)+/g,'');if(/^[\],:{}\s]*$/.test(filtered)){return eval('('+src+')');}else{throw new SyntaxError('Error parsing JSON, source is not valid.');}};$.quoteString=function(string){if(string.match(escapeable)){return'"'+string.replace(escapeable,function(a){var c=meta[a];if(typeof c==='string'){return c;}
c=a.charCodeAt();return'\\u00'+Math.floor(c/16).toString(16)+(c%16).toString(16);})+'"';}
return'"'+string+'"';};})(jQuery);

/**
 * http://www.jstorage.info
 */
(function(g){function m(){if(e.jStorage)try{c=n(""+e.jStorage)}catch(a){e.jStorage="{}"}else e.jStorage="{}";j=e.jStorage?(""+e.jStorage).length:0}function h(){try{e.jStorage=o(c),d&&(d.setAttribute("jStorage",e.jStorage),d.save("jStorage")),j=e.jStorage?(""+e.jStorage).length:0}catch(a){}}function i(a){if(!a||"string"!=typeof a&&"number"!=typeof a)throw new TypeError("Key name must be string or numeric");if("__jstorage_meta"==a)throw new TypeError("Reserved key name");return!0}function k(){var a,
	b,d,e=Infinity,f=!1;clearTimeout(p);if(c.__jstorage_meta&&"object"==typeof c.__jstorage_meta.TTL){a=+new Date;d=c.__jstorage_meta.TTL;for(b in d)d.hasOwnProperty(b)&&(d[b]<=a?(delete d[b],delete c[b],f=!0):d[b]<e&&(e=d[b]));Infinity!=e&&(p=setTimeout(k,e-a));f&&h()}}if(!g||!g.toJSON&&!Object.toJSON&&!window.JSON)throw Error("jQuery, MooTools or Prototype needs to be loaded before jStorage!");var c={},e={jStorage:"{}"},d=null,j=0,o=g.toJSON||Object.toJSON||window.JSON&&(JSON.encode||JSON.stringify),
	n=g.evalJSON||window.JSON&&(JSON.decode||JSON.parse)||function(a){return(""+a).evalJSON()},f=!1,p,l={isXML:function(a){return(a=(a?a.ownerDocument||a:0).documentElement)?"HTML"!==a.nodeName:!1},encode:function(a){if(!this.isXML(a))return!1;try{return(new XMLSerializer).serializeToString(a)}catch(b){try{return a.xml}catch(c){}}return!1},decode:function(a){var b="DOMParser"in window&&(new DOMParser).parseFromString||window.ActiveXObject&&function(a){var b=new ActiveXObject("Microsoft.XMLDOM");b.async=
	"false";b.loadXML(a);return b};if(!b)return!1;a=b.call("DOMParser"in window&&new DOMParser||window,a,"text/xml");return this.isXML(a)?a:!1}};g.jStorage={version:"0.1.6.1",set:function(a,b){i(a);if(l.isXML(b))b={_is_xml:!0,xml:l.encode(b)};else{if("function"==typeof b)return;b&&"object"==typeof b&&(b=n(o(b)))}c[a]=b;h();return b},get:function(a,b){i(a);return a in c?c[a]&&"object"==typeof c[a]&&c[a]._is_xml&&c[a]._is_xml?l.decode(c[a].xml):c[a]:"undefined"==typeof b?null:b},deleteKey:function(a){i(a);
	return a in c?(delete c[a],c.__jstorage_meta&&"object"==typeof c.__jstorage_meta.TTL&&a in c.__jstorage_meta.TTL&&delete c.__jstorage_meta.TTL[a],h(),!0):!1},setTTL:function(a,b){var d=+new Date;i(a);b=Number(b)||0;if(a in c){if(!c.__jstorage_meta)c.__jstorage_meta={};if(!c.__jstorage_meta.TTL)c.__jstorage_meta.TTL={};0<b?c.__jstorage_meta.TTL[a]=d+b:delete c.__jstorage_meta.TTL[a];h();k();return!0}return!1},flush:function(){c={};h();return!0},storageObj:function(){function a(){}a.prototype=c;return new a},
	index:function(){var a=[],b;for(b in c)c.hasOwnProperty(b)&&"__jstorage_meta"!=b&&a.push(b);return a},storageSize:function(){return j},currentBackend:function(){return f},storageAvailable:function(){return!!f},reInit:function(){var a;if(d&&d.addBehavior){a=document.createElement("link");d.parentNode.replaceChild(a,d);d=a;d.style.behavior="url(#default#userData)";document.getElementsByTagName("head")[0].appendChild(d);d.load("jStorage");a="{}";try{a=d.getAttribute("jStorage")}catch(b){}e.jStorage=
	a;f="userDataBehavior"}m()}};(function(){var a=!1;if("localStorage"in window)try{window.localStorage.setItem("_tmptest","tmpval"),a=!0,window.localStorage.removeItem("_tmptest")}catch(b){}if(a)try{if(window.localStorage)e=window.localStorage,f="localStorage"}catch(c){}else if("globalStorage"in window)try{window.globalStorage&&(e=window.globalStorage[window.location.hostname],f="globalStorage")}catch(g){}else if(d=document.createElement("link"),d.addBehavior){d.style.behavior="url(#default#userData)";
	document.getElementsByTagName("head")[0].appendChild(d);d.load("jStorage");a="{}";try{a=d.getAttribute("jStorage")}catch(h){}e.jStorage=a;f="userDataBehavior"}else{d=null;return}m();k()})()})(window.jQuery||window.$);

/**
 * Copyright (c) 2011-2012 Björn Kahlert (http://bkahlert.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 **/
(function($) {	
	function setLastFingerprint(fingerprint) {
		$.jStorage.set("lastFingerprint", fingerprint);
	}
	
	function getLastFingerprint() {
		return $.jStorage.get("lastFingerprint", $.fingerprint());
	}
	
	function updateFingerprint() {
		var lastFingerprint = getLastFingerprint();
		var fingerprint = $.fingerprint();
		
		// TODO
		alert("old: " + lastFingerprint + "; new: " + fingerprint);
		
		if(lastFingerprint != fingerprint) {
			$.ajax({
				url: ('https:' == document.location.protocol ? 'https://' : 'http://') + "${HOST}${CONTEXT_PATH}/rest/fingerprint/!" + lastFingerprint + "/associate/!" + fingerprint,
				type: "GET",
				dataType: "script"
			});
			setLastFingerprint(fingerprint);
		}
	}
	
	function log(e) {
		var date = new Date().format("yyyy-mm-dd'T'HH:MM:ss.lo");
		var iso8601date = date.substring(0, date.length-2) + ":" + date.substr(date.length-2);
		$.ajax({
			url: ('https:' == document.location.protocol ? 'https://' : 'http://') + "${HOST}${CONTEXT_PATH}/rest/doclog/!" + $.fingerprint(),
			data: {
				id: $.parseQuery().id,
				url: window.location.href,
				dateTime: new Date().format("yyyy-mm-dd'T'HH:MM:ss.lo"),
				event: e,
				"bounds.x": $(window).scrollLeft(),
				"bounds.y": $(window).scrollTop(),
				"bounds.width": $(window).width(),
				"bounds.height": $(window).height()
			},
			type: "POST",
			dataType: "script"
		});
	}
	
	$(document).ready(function() {
		updateFingerprint();
		log("ready");
		
		var token = $.parseQuery().token;
		if(token) log("survey-" + token);

		$("a").click(function(e) {
			log("link-" + this.href);
		});
		
		$("input").keydown(function(e) {
			var id = this.id ? this.id : (this.name ? this.name : "");
			log("typing-" + id + "-" + $(this).val());
		});
		
		$("textarea").keydown(function(e) {
			var id = this.id ? this.id : (this.name ? this.name : "");
			log("typing-" + id + "-" + $(this).val());
		});
		
		$(window).focus(function() {
			log("focus")
	    });
		
		var scrollTimerID;
		$(window).scroll(function() {
			if(scrollTimerID) window.clearTimeout(scrollTimerID);
			scrollTimerID = window.setTimeout(function() { log("scroll"); }, 2000);
		});
		
		var resizeTimerID;
		$(window).resize(function() {
			if(resizeTimerID) window.clearTimeout(resizeTimerID);
			resizeTimerID = window.setTimeout(function() { log("resize"); }, 1500);
		});

	    $(window).blur(function() {
	    	log("blur")
	    });
	    
	    $(window).unload(function() {
			log("unload");
		});
	});
})(jQuery);