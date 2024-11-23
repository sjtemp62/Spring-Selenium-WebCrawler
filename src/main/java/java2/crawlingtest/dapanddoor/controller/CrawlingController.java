package java2.crawlingtest.dapanddoor.controller;

import java2.crawlingtest.dapanddoor.service.DapNoticeCrawlingService;
import java2.crawlingtest.dapanddoor.service.DoorToDoListCrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CrawlingController {
    private final DapNoticeCrawlingService dapNoticeCrawlingService;
    private final DoorToDoListCrawlingService doorToDoListCrawlingService;

    @GetMapping("/dap-noitice-crawling")
    public String dapNoticeloginAndScrape(@RequestParam String loginUrl,
                                          @RequestParam String username,
                                          @RequestParam String password) {
        return dapNoticeCrawlingService.performCrawling(loginUrl, username, password);
    }

    @GetMapping("/door-todolist-crawling")
    public String doorToDoListloginAndScrape(@RequestParam String loginUrl,
                                 @RequestParam String username,
                                 @RequestParam String password) {
        return doorToDoListCrawlingService.performCrawling(loginUrl, username, password);
    }
}
