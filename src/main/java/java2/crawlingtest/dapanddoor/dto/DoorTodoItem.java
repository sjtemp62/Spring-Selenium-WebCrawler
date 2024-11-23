package java2.crawlingtest.dapanddoor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoorTodoItem {
    @JsonProperty("title")
    private String title;

    @JsonProperty("due_date")
    private String dueDate;

    @JsonProperty("url")
    private String url;
}