package meeting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Comment {

    private long id;

    private String username;

    private String message;

    private LocalDateTime postDate;
}
