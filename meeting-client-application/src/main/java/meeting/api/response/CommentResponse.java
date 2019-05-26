package meeting.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentResponse {

    private long id;

    private String username;

    private String message;

    private String postDate;
}
