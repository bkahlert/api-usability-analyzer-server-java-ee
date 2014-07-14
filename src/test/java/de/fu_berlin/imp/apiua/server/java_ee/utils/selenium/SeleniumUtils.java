package de.fu_berlin.imp.apiua.server.java_ee.utils.selenium;

import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import de.fu_berlin.imp.apiua.server.java_ee.utils.Utils;

public class SeleniumUtils {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(SeleniumUtils.class);

	private static final String injectedScrollToPluginMarker = SeleniumUtils.class
			.getSimpleName() + "ScrollToPluginInjected";

	public static interface IWebDriverFactory {
		public WebDriver create();

		public boolean supportsAuthentication();
	}

	public static IWebDriverFactory getDefaultFirefoxDriverFactory() {
		return new IWebDriverFactory() {
			@Override
			public WebDriver create() {
				return new FirefoxDriver();
			}

			@Override
			public boolean supportsAuthentication() {
				return true;
			}

			@Override
			public String toString() {
				return FirefoxDriver.class.getSimpleName();
			}
		};
	}

	public static IWebDriverFactory getSuffixedFirefoxDriverFactory(
			final String suffix) {
		return new IWebDriverFactory() {
			@Override
			public WebDriver create() {
				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference(
						"general.useragent.override",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:20.0) Gecko/20100101 Firefox/20.0; "
								+ suffix);
				return new FirefoxDriver(profile);
			}

			@Override
			public boolean supportsAuthentication() {
				return true;
			}

			@Override
			public String toString() {
				return FirefoxDriver.class.getSimpleName();
			}
		};
	}

	public static IWebDriverFactory getDefaultSafariDriverFactory() {
		return new IWebDriverFactory() {
			@Override
			public WebDriver create() {
				return new SafariDriver();
			}

			@Override
			public boolean supportsAuthentication() {
				return false;
			}

			@Override
			public String toString() {
				return SafariDriver.class.getSimpleName();
			}
		};
	}

	public static IWebDriverFactory getDefaultChromeDriverFactory() {
		return new IWebDriverFactory() {
			@Override
			public WebDriver create() {
				return new ChromeDriver();
			}

			@Override
			public boolean supportsAuthentication() {
				return true;
			}

			@Override
			public String toString() {
				return ChromeDriver.class.getSimpleName();
			}
		};
	}

	public static IWebDriverFactory getVirginChromeDriverFactory(
			final String seed) {
		return new IWebDriverFactory() {
			@Override
			public WebDriver create() {
				String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31 "
						+ (seed == null ? Utils.getRandomString(32) : seed);

				ChromeOptions options = new ChromeOptions();
				options.addArguments("--user-agent=\"" + userAgent + "\"");

				DesiredCapabilities capabilities = DesiredCapabilities.chrome();
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				return new ChromeDriver(capabilities);
			}

			@Override
			public boolean supportsAuthentication() {
				return true;
			}

			@Override
			public String toString() {
				return ChromeDriver.class.getSimpleName();
			}
		};
	}

	/**
	 * Injects the jQuery scrollTo plugin.
	 * 
	 * @param driver
	 * @see <a
	 *      href="http://flesler.com/jquery/scrollTo/">http://flesler.com/jquery/scrollTo/</a>
	 */
	public static void injectScrollToPlugin(WebDriver driver) {
		executeScript(
				driver,
				";(function($){var h=$.scrollTo=function(a,b,c){$(window).scrollTo(a,b,c)};h.defaults={axis:'xy',duration:parseFloat($.fn.jquery)>=1.3?0:1,limit:true};h.window=function(a){return $(window)._scrollable()};$.fn._scrollable=function(){return this.map(function(){var a=this,isWin=!a.nodeName||$.inArray(a.nodeName.toLowerCase(),['iframe','#document','html','body'])!=-1;if(!isWin)return a;var b=(a.contentWindow||a).document||a.ownerDocument||a;return/webkit/i.test(navigator.userAgent)||b.compatMode=='BackCompat'?b.body:b.documentElement})};$.fn.scrollTo=function(e,f,g){if(typeof f=='object'){g=f;f=0}if(typeof g=='function')g={onAfter:g};if(e=='max')e=9e9;g=$.extend({},h.defaults,g);f=f||g.duration;g.queue=g.queue&&g.axis.length>1;if(g.queue)f/=2;g.offset=both(g.offset);g.over=both(g.over);return this._scrollable().each(function(){if(e==null)return;var d=this,$elem=$(d),targ=e,toff,attr={},win=$elem.is('html,body');switch(typeof targ){case'number':case'string':if(/^([+-]=)?\\d+(\\.\\d+)?(px|%)?$/.test(targ)){targ=both(targ);break}targ=$(targ,this);if(!targ.length)return;case'object':if(targ.is||targ.style)toff=(targ=$(targ)).offset()}$.each(g.axis.split(''),function(i,a){var b=a=='x'?'Left':'Top',pos=b.toLowerCase(),key='scroll'+b,old=d[key],max=h.max(d,a);if(toff){attr[key]=toff[pos]+(win?0:old-$elem.offset()[pos]);if(g.margin){attr[key]-=parseInt(targ.css('margin'+b))||0;attr[key]-=parseInt(targ.css('border'+b+'Width'))||0}attr[key]+=g.offset[pos]||0;if(g.over[pos])attr[key]+=targ[a=='x'?'width':'height']()*g.over[pos]}else{var c=targ[pos];attr[key]=c.slice&&c.slice(-1)=='%'?parseFloat(c)/100*max:c}if(g.limit&&/^\\d+$/.test(attr[key]))attr[key]=attr[key]<=0?0:Math.min(attr[key],max);if(!i&&g.queue){if(old!=attr[key])animate(g.onAfterFirst);delete attr[key]}});animate(g.onAfter);function animate(a){$elem.animate(attr,f,g.easing,a&&function(){a.call(this,e,g)})}}).end()};h.max=function(a,b){var c=b=='x'?'Width':'Height',scroll='scroll'+c;if(!$(a).is('html,body'))return a[scroll]-$(a)[c.toLowerCase()]();var d='client'+c,html=a.ownerDocument.documentElement,body=a.ownerDocument.body;return Math.max(html[scroll],body[scroll])-Math.min(html[d],body[d])};function both(a){return typeof a=='object'?a:{top:a,left:a}}})(jQuery);");
		executeScript(driver, "window[\"" + injectedScrollToPluginMarker
				+ "\"] = true;");
	}

	/**
	 * Returns true if the jQuery scrollTo plugin is already injected and can be
	 * used.
	 * 
	 * @param driver
	 * @return
	 */
	public static boolean isScrollToPluginInjected(WebDriver driver) {
		return (Boolean) executeScript(driver, "return window[\""
				+ injectedScrollToPluginMarker + "\"] == true;");
	}

	/**
	 * Scrolls to the given position.
	 * <p>
	 * <strong>This
	 * 
	 * @param driver
	 */
	public static void scrollTo(WebDriver driver, int toX, int toY) {
		if (!isScrollToPluginInjected(driver)) {
			SeleniumUtils.injectScrollToPlugin(driver);
		}
		String script = "$(window).scrollTo({top:'" + toY + "px', left:'" + toX
				+ "px'}, 0)";
		executeScript(driver, script);
	}

	/**
	 * Returns the amount of horizontal space in pixels that is added to the
	 * viewport's width.
	 * 
	 * @param driver
	 * @return
	 */
	public static long getHorizontalTrim(WebDriver driver) {
		return (Long) executeScript(driver,
				"return parseInt(top.outerWidth)-parseInt(top.innerWidth);");
	}

	/**
	 * Returns the amount of vertical space in pixels that is added to the
	 * viewport's height.
	 * 
	 * @param driver
	 * @return
	 */
	public static long getVerticalTrim(WebDriver driver) {
		return (Long) executeScript(driver,
				"return parseInt(top.outerHeight)-parseInt(top.innerHeight);");
	}

	/**
	 * Sets the currently displayed window's inner size.
	 * 
	 * @param driver
	 * @param innerSize
	 */
	public static void setInnerSize(WebDriver driver, Dimension innerSize) {
		if (innerSize == null)
			throw new IllegalArgumentException();
		driver.manage().window().setPosition(new Point(0, 0));

		Dimension outerSize = new Dimension(
				(int) (innerSize.width + getHorizontalTrim(driver)),
				(int) (innerSize.height + getVerticalTrim(driver)));
		driver.manage().window().setSize(outerSize);
	}

	public static Object executeScript(WebDriver driver, String script) {
		return executeScript(driver, script, new Object[0]);
	}

	public static Object executeScript(WebDriver driver, String script,
			Object... args) {
		return ((JavascriptExecutor) driver).executeScript(script, args);
	}

	public static String openWindow(WebDriver driver, String url) {
		String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
		Object element = executeScript(driver, String.format(script, url));
		if (element instanceof WebElement) {
			WebElement anchor = (WebElement) element;
			anchor.click();
			executeScript(driver,
					"var a=arguments[0];a.parentNode.removeChild(a);", anchor);
			Set<String> handles = driver.getWindowHandles();
			String current = driver.getWindowHandle();
			handles.remove(current);
			return handles.iterator().next();
		} else {
			throw new WebDriverException("Unable to open window");
		}
	}

	/**
	 * Returns the {@link WebElement}'s ID. If it is not defined the name is
	 * returned. If neither the ID nor the name is defined <code>null</code> is
	 * returned.
	 * 
	 * @param element
	 * @return
	 */
	public static String getIdOrName(WebElement element) {
		String id = element.getAttribute("id");
		if (id != null && !id.isEmpty())
			return id;
		String name = element.getAttribute("name");
		if (name != null && !name.isEmpty())
			return name;

		return null;
	}

}
