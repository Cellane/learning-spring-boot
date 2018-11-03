package net.milanvit.learningspringboot.webdriver;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class WebDriverAutoConfigurationTests {
    private AnnotationConfigApplicationContext context;

    @After
    public void close() {
        if (context != null) {
            context.close();
        }
    }

    private void load(Class<?>[] configs, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        applicationContext.register(WebDriverAutoConfiguration.class);

        if (configs.length > 0) {
            applicationContext.register(configs);
        }

        TestPropertyValues.of(environment).applyTo(applicationContext);
        applicationContext.refresh();

        context = applicationContext;
    }

    @Test
    public void fallbackToNonGuiModeWhenAllBrowsersDisabled() {
        load(new Class[]{},
            "net.milanvit.webdriver.firefox.enabled:false",
            "net.milanvit.webdriver.safari.enabled:false",
            "net.milanvit.webdriver.chrome.enabled:false");

        WebDriver driver = context.getBean(WebDriver.class);
        assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isFalse();
        assertThat(ClassUtils.isAssignable(HtmlUnitDriver.class, driver.getClass())).isTrue();
    }

    @Test
    public void testWithMockedFirefox() {
        load(new Class[]{MockFirefoxConfiguration.class},
            "net.milanvit.webdriver.safari.enabled:false",
            "net.milanvit.webdriver.chrome.enabled:false");

        WebDriver driver = context.getBean(WebDriver.class);
        assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
        assertThat(ClassUtils.isAssignable(FirefoxDriver.class, driver.getClass())).isTrue();
    }

    @Test
    public void testWithMockedSafari() {
        load(new Class[]{MockSafariConfiguration.class},
            "net.milanvit.webdriver.firefox.enabled:false",
            "net.milanvit.webdriver.chrome.enabled:false");

        WebDriver driver = context.getBean(WebDriver.class);
        assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
        assertThat(ClassUtils.isAssignable(SafariDriver.class, driver.getClass())).isTrue();
    }

    @Test
    public void testWithMockedChrome() {
        load(new Class[]{MockChromeConfiguration.class},
            "net.milanvit.webdriver.firefox.enabled:false",
            "net.milanvit.webdriver.safari.enabled:false");

        WebDriver driver = context.getBean(WebDriver.class);
        assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
        assertThat(ClassUtils.isAssignable(ChromeDriver.class, driver.getClass())).isTrue();
    }

    @Configuration
    protected static class MockFirefoxConfiguration {
        @Bean
        FirefoxDriverFactory firefoxDriverFactory() {
            FirefoxDriverFactory factory = mock(FirefoxDriverFactory.class);

            given(factory.getObject()).willReturn(mock(FirefoxDriver.class));

            return factory;
        }
    }

    @Configuration
    protected static class MockSafariConfiguration {
        @Bean
        SafariDriverFactory safariDriverFactory() {
            SafariDriverFactory factory = mock(SafariDriverFactory.class);

            given(factory.getObject()).willReturn(mock(SafariDriver.class));

            return factory;
        }
    }

    @Configuration
    protected static class MockChromeConfiguration {
        @Bean
        ChromeDriverFactory chromeDriverFactory() {
            ChromeDriverFactory factory = mock(ChromeDriverFactory.class);

            given(factory.getObject()).willReturn(mock(ChromeDriver.class));

            return factory;
        }
    }
}
