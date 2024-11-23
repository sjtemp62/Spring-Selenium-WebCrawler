package java2.crawlingtest.dapanddoor.service;

import java2.crawlingtest.dapanddoor.dto.DoorTodoItem;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoorToDoListCrawlingService extends AbstractCrawlingService{
    @Override
    protected void login(String loginUrl, String username, String password) {
        try {
            driver.get(loginUrl);

            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uid")));
            WebElement passwordInput = driver.findElement(By.id("upw"));
            WebElement loginButton = driver.findElement(By.cssSelector(".btnLogin"));

            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            loginButton.click();


            handlePopupsIfNecessary(false);
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            throw new RuntimeException("Failed to log in", e);
        }
    }

    @Override
    protected String crawl() {
        List<DoorTodoItem> doorTodoItems = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        try {
            // 공지사항 페이지로 이동
            driver.get("https://door.deu.ac.kr/");

            // 최상위 요소 'list_box todo_list'가 보일 때까지 대기
            WebElement todoListBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".list_box.todo_list")
            ));
            // To-Do 항목들 가져오기
            List<WebElement> todoItemsElements = todoListBox.findElements(By.cssSelector(".todo_items"));

            for (WebElement item : todoItemsElements) {
                // 과제 제목
                String title = item.findElement(By.cssSelector(".cnt_tit .tit")).getText();

                // 제출 기한
                String dueDate = item.findElement(By.cssSelector(".cnt_date span")).getText();

                // 링크 추출
                String onClickAttribute = item.findElement(By.tagName("a")).getAttribute("onclick");
                String url = extractUrlFromOnClick(onClickAttribute);

                // 데이터 객체로 저장
                doorTodoItems.add(new DoorTodoItem(title, dueDate, url));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(doorTodoItems);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"An error occurred during crawling.\"}";
        }
    }

    // onClick 속성에서 URL 추출
    private String extractUrlFromOnClick(String onClickAttribute) {
        if (onClickAttribute == null || !onClickAttribute.contains("gotodolist(")) {
            return "";
        }
        return onClickAttribute.split("'")[1]; // '/CRoom/Estimation/75138'와 같은 URL 부분 추출
    }
}
