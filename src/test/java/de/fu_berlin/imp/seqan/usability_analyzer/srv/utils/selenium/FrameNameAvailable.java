package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class FrameNameAvailable implements ExpectedCondition<String> {
	private String cssSelector;

	public FrameNameAvailable(String cssSelector) {
		if (cssSelector == null)
			throw new IllegalArgumentException();
		this.cssSelector = cssSelector;
	}

	public String apply(WebDriver driver) {
		try {
			return (String) SeleniumUtils.executeScript(driver, "return $(\""
					+ cssSelector + "\").get(0).contentWindow.name;");
		} catch (WebDriverException e) {
			// very probably due to a denied access of the name property; this
			// happens if the SUAsrv has not redirected yet to the callee page
			return null;
		}
	}
}
