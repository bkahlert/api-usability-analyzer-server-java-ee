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

/*
 * Author: Björn Kahlert
 */
window["APIUAactivateLogging"] = function(fingerprint) {
	$.ajax({
		url: (("${HOST}".indexOf(":") >= 0) ? "http://" : "https://") + "${HOST}${CONTEXT_PATH}/rest/fingerprint/!" + fingerprint,
		type: "GET",
		dataType: "jsonp",
		success: function(data, status) {
			if(data.identifier) {
				$(".APIUAid").html(data.identifier);
				document.title = "ID: " + data.identifier;
			}
        },
        error: function(request, status, error) {
            alert(request.responseText);
            alert(status);
            alert(error);
        }    
	});
}
$(document).ready(function() {
	var id = $.parseQuery().id;
	if($.parseQuery().APIUAid) id = $.parseQuery().APIUAid;
	if(id) {
		$(".APIUAid").html(id);
	}
});