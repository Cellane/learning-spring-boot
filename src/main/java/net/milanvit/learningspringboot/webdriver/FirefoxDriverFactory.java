package net.milanvit.learningspringboot.webdriver;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

class FirefoxDriverFactory implements ObjectFactory<FirefoxDriver> {
    private WebDriverConfigurationProperties properties;

    FirefoxDriverFactory(WebDriverConfigurationProperties properties) {
        this.properties = properties;
    }

    @Override
    public FirefoxDriver getObject() throws BeansException {
        if (properties.getFirefox().isEnabled()) {
            try {
                return new FirefoxDriver();
            } catch (WebDriverException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
