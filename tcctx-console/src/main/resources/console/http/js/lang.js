//$.namespace("tcctx.lang");
var tcctx = tcctx ? tcctx :  new Object();
tcctx.lang = function() {
	var LANG_EN = 0;
	var LANG_CN = 1;
	var lang = {
		'TimeoutTableHeader' : ['Timeout items table','超时任务列表'],
        'TimeoutTableDesc' : ['The submitted transaction entities, which are timeout and waiting for retry...', '已经提交的事务实体，处理超时等待重试中...'],

        'IDTbHeader' : ['ID', '主键'],
        'StateHeader' : ['State', '状态'],
        'HandleTimeHeader' : ['Handle Time', '处理时间'],
		'GetMoreData' : ['Get More Data', '获取更多数据']
	};
	
	var COOKIE_LANG_NAME = "cookie_lang";
	
	function log(str) {
		if (typeof (console) != 'undefined' && typeof(console.log) != 'undefined') {
			console.log(str);
		} else {
			$('body').append('<input type="hidden" value="' + str + " />");
		}
	}
	
	function setCookie(name,value,expires,path,domain,secure)
	{
		var expDays = expires*24*60*60*1000;
		var expDate = new Date();
		expDate.setTime(expDate.getTime()+expDays);
		var expString = ((expires==null) ? "": (";expires="+expDate.toGMTString()));
		var pathString = ((path==null) ? "": (";path="+path));
		var domainString = ((domain==null) ? "": (";domain="+domain));
		var secureString = ((secure==true) ? ";secure": "");
		document.cookie = name + "="+ escape(value) + expString + pathString + domainString + secureString;
	}

	function getCookie(name)
	{
		var result = null;
		var myCookie = document.cookie + ";";
		var searchName = name + "=";
		var startOfCookie = myCookie.indexOf(searchName);
		var endOfCookie;
		if (startOfCookie != -1)
		{
			startOfCookie += searchName.length;
			endOfCookie = myCookie.indexOf(";",startOfCookie);
			result = unescape(myCookie.substring(startOfCookie,endOfCookie));
		}
		return result;
	}
	
	function setText($obj) {
		var key = $obj.attr('langKey');
		if (typeof(lang[key]) != 'undefined') {
			var text = lang[key][tcctx.lang.langNow];
			$obj.text(lang[key][tcctx.lang.langNow]);
		} else {
			log('key [' + key + '] not found');
		}
	}
	function setTitle($obj) {
		var key = $obj.attr('langKey');
		if (typeof(lang[key]) != 'undefined') {
			var title = lang[key][tcctx.lang.langNow];
			$obj.attr('title', title);
		} else {
			log('key [' + key + '] not found');
		}
	}
	
	return {
		langNow : LANG_CN,
		EVENT_LOAD_FINISHED : 'loadFinished',
		init : function(langNow) {
			if (typeof(langNow) != 'undefined') {
				this.setLangType(langNow);
			} else {
				var langInCookie = getCookie(COOKIE_LANG_NAME);
				if (langInCookie == LANG_CN || langInCookie == LANG_EN) {
					this.setLangType(langInCookie);
				}
			}
			$(document).on(this.EVENT_LOAD_FINISHED, '.lang', function() {
				log('load lang');
				setText($(this));
			});
			$(document).on(this.EVENT_LOAD_FINISHED, '.langTitle', function() {
				log('load title');
				setTitle($(this));
			});
			this.trigger();
			
			$(document).on('click','.langSelector',function() {
				var langSelected = $(this).attr('langNow');
                tcctx.lang.setLangType(langSelected);
                tcctx.lang.trigger();
				return false;
			});
		},
		setLangType : function (langNow) {
			this.langNow = langNow;
			setCookie(COOKIE_LANG_NAME,langNow,30,'/');
		},
		getLangType : function () {
			return this.langNow;
		},
		show : function($parent) {
			var $obj;
			var $objTitle;
			if ($parent) {
				$obj = $parent.find('.lang');
				$objTitle = $parent.find('.langTitle');
			} else {
				$obj = $('.lang');
				$objTitle = $('.langTitle');
			}
			$obj.each(function() {
				setText($(this));
			});
			$objTitle.each(function() {
				setTitle($(this));
			});
		},
		trigger : function() {
			log('to load lang now');
			$('.lang').trigger(this.EVENT_LOAD_FINISHED);//触发语言显示事件
			$('.langTitle').trigger(this.EVENT_LOAD_FINISHED);//触发语言显示事件
		}
	}
}();
