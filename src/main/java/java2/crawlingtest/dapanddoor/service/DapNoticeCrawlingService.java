package java2.crawlingtest.dapanddoor.service;

import java2.crawlingtest.dapanddoor.dto.DapNoticeItem;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.ArrayList;
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
        List<DapNoticeItem> notices = new ArrayList<>();

        try {
            // 공지사항 페이지로 이동
            driver.get("https://dap.deu.ac.kr/StdNotice.aspx");

            // 공지사항 링크 가져오기 (1페이지)
            wait.until(ExpectedConditions.attributeContains(
                    By.cssSelector("a[href*='StdNotice02.aspx']"), "href", "PageNo=1"
            ));
            List<WebElement> noticeLinks = driver.findElements(By.cssSelector("a[href*='StdNotice02.aspx']"));

            for (WebElement link : noticeLinks) {
                String linkText = link.getText(); // 공지사항 제목
                String linkHref = link.getAttribute("href"); // 공지사항 링크
                String absoluteUrl = new URL(new URL(driver.getCurrentUrl()), linkHref).toString();

                notices.add(new DapNoticeItem(absoluteUrl, linkText));
            }

            // 다음 페이지 (2페이지)로 이동
            WebElement page2Button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("CP1_COM_Page_Controllor1_lbtnPage2")
            ));
            page2Button.click();

            // 2페이지 공지사항 가져오기
            wait.until(ExpectedConditions.attributeContains(
                    By.cssSelector("a[href*='StdNotice02.aspx']"), "href", "PageNo=2"
            ));
            noticeLinks = driver.findElements(By.cssSelector("a[href*='StdNotice02.aspx']"));

            for (WebElement link : noticeLinks) {
                String linkText = link.getText();
                String linkHref = link.getAttribute("href");
                String absoluteUrl = new URL(new URL(driver.getCurrentUrl()), linkHref).toString();

                notices.add(new DapNoticeItem(absoluteUrl, linkText));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Jackson으로 JSON 변환
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(notices); // JSON 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to convert notices to JSON.\"}";
        }
    }
}