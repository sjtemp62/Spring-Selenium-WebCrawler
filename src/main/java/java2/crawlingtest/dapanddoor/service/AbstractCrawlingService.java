package java2.crawlingtest.dapanddoor.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;

public abstract class AbstractCrawlingService implements CrawlingService {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @Override
    public String performCrawling(String loginUrl, String username, String password) {
        try {
            initializeDriver();

            // 공통: 로그인
            login(loginUrl, username, password);

            // 페이지별 크롤링 로직 실행
            String result = crawl();

//            cleanupDriver(); // 리소스 정리
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            cleanupDriver();
            return "Error occurred: " + e.getMessage();
        }
    }

    private void initializeDriver() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected abstract void login(String loginUrl, String username, String password);

    protected void handlePopupsIfNecessary(boolean shouldHandlePopups) {
        if (!shouldHandlePopups) {
            System.out.println("Popup handling skipped for this page.");
            return;
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    webDriver -> webDriver.getWindowHandles().size() > 1
            );
            System.out.println("Popup detected!");
        } catch (TimeoutException e) {
            System.out.println("No popup appeared after login.");
        }

        String mainWindowHandle = driver.getWindowHandle(); // 메인 창 핸들 저장

        try {
            while (true) {
                Set<String> allWindowHandles = driver.getWindowHandles();

                if (allWindowHandles.size() == 1) {
                    break; // 팝업이 없으면 종료
                }

                for (String handle : allWindowHandles) {
                    if (!handle.equals(mainWindowHandle)) {
                        try {
                            // 팝업으로 전환
                            driver.switchTo().window(handle);

                            // 팝업 URL 또는 제목 확인
                            String popupUrl = driver.getCurrentUrl();
                            String popupTitle = driver.getTitle();
                            System.out.println("Closing popup: " + popupUrl + " | Title: " + popupTitle);

                            // 팝업 닫기
                            driver.close();
                        } catch (NoSuchWindowException e) {
                            System.err.println("Window already closed: " + handle);
                        } catch (Exception e) {
                            System.err.println("Error while closing popup: " + e.getMessage());
                        }
                    }
                }

                // 메인 창으로 복귀
                driver.switchTo().window(mainWindowHandle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 메인 창으로 전환 보장
            driver.switchTo().window(mainWindowHandle);
        }
    }

    protected abstract String crawl(); // 각 페이지별 크롤링 로직은 하위 클래스에서 구현

    private void cleanupDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
