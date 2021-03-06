package de.fu_berlin.imp.apiua.server.java_ee.utils.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class DocumentCompletedLoading implements ExpectedCondition<Boolean> {

	public Boolean apply(WebDriver driver) {
		return (Boolean) SeleniumUtils.executeScript(driver,
				"return document.readyState == 'complete';");

	}

}
