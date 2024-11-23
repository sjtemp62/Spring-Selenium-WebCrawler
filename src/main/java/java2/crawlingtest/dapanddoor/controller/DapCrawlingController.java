package java2.crawlingtest.dapanddoor.controller;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class DapCrawlingController {

    @GetMapping("/single-dap-crawling")
    public String loginAndScrape(@RequestParam String loginUrl,
                                 @RequestParam String username,
                                 @RequestParam String password) {
        try {
            // Selenium Server URL 설정
            URL seleniumServerUrl = new URL("http://localhost:4444/wd/hub");

            // Headless 모드 설정 (필요 시 주석 처리)
            ChromeOptions options = new ChromeOptions();
            // options.addArguments("--headless");
            options.addArguments("--disable-gpu");

            // RemoteWebDriver를 통해 Selenium Server에 연결
            WebDriver driver = new RemoteWebDriver(seleniumServerUrl, options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // 로그인 페이지로 이동
            driver.get(loginUrl);

            // 로그인 입력 필드와 로그인 버튼 찾기
            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id")));
            WebElement passwordInput = driver.findElement(By.id("pw"));
            WebElement loginButton = driver.findElement(By.id("btn-login"));

            // 로그인 입력 후 버튼 클릭
            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            loginButton.click();

            // 팝업 대기 로직 추가
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

            // 로그인 후 공지사항 페이지로 이동
            driver.get("https://dap.deu.ac.kr/StdNotice.aspx");

            // 공지사항 링크 가져오기 (1페이지)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='StdNotice02.aspx']")));
            List<WebElement> noticeLinks = driver.findElements(By.cssSelector("a[href*='StdNotice02.aspx']"));

            StringBuilder result = new StringBuilder();
            for (WebElement link : noticeLinks) {
                String linkText = link.getText();
                String linkHref = link.getAttribute("href");

                // 상대 경로를 절대 경로로 변환
                String absoluteUrl = new URL(new URL(driver.getCurrentUrl()), linkHref).toString();

                result.append("Link: ").append(absoluteUrl).append("\n");
                result.append("Text: ").append(linkText).append("\n\n");
            }

            // JavaScript 실행 대신 WebElement 클릭
            WebElement page2Button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("CP1_COM_Page_Controllor1_lbtnPage2")));
            page2Button.click();

            // 페이지 전환 대기
            wait.until(ExpectedConditions.attributeContains(
                    By.cssSelector("a[href*='StdNotice02.aspx']"), "href", "PageNo=2"
            ));

            // 2페이지의 공지사항 링크 가져오기
            noticeLinks = driver.findElements(By.cssSelector("a[href*='StdNotice02.aspx']"));
            for (WebElement link : noticeLinks) {
                String linkText = link.getText();
                String linkHref = link.getAttribute("href");
                String absoluteUrl = new URL(new URL(driver.getCurrentUrl()), linkHref).toString();
                result.append("Link: ").append(absoluteUrl).append("\n");
                result.append("Text: ").append(linkText).append("\n\n");
            }
//            // 모든 창 닫기
//            allWindowHandles = driver.getWindowHandles();
//            for (String handle : allWindowHandles) {
//                driver.switchTo().window(handle);
//                driver.close();
//            }

            return result.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Error: Invalid Selenium Server URL";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }
}
