package java2.crawlingtest.dapanddoor.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;

@Service
public class DapNoticeCrawlingService extends AbstractCrawlingService {

    @Override
    protected void login(String loginUrl, String username, String password) {
        try {
            driver.get(loginUrl);

            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id")));
            WebElement passwordInput = driver.findElement(By.id("pw"));
            WebElement loginButton = driver.findElement(By.id("btn-login"));

            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            loginButton.click();


            handlePopupsIfNecessary(true);
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            throw new RuntimeException("Failed to log in", e);
        }
    }

    @Override
    protected String crawl() {
        StringBuilder result = new StringBuilder();

        try {
            // 공지사항 페이지로 이동
            driver.get("https://dap.deu.ac.kr/StdNotice.aspx");

            // 공지사항 링크 가져오기 (1페이지)
            wait.until(ExpectedConditions.attributeContains(
                    By.cssSelector("a[href*='StdNotice02.aspx']"), "href", "PageNo=1"
            ));
            List<WebElement> noticeLinks = driver.findElements(By.cssSelector("a[href*='StdNotice02.aspx']"));

            for (WebElement link : noticeLinks) {
                String linkText = link.getText();
                String linkHref = link.getAttribute("href");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}