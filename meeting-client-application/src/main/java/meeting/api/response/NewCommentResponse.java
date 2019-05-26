package meeting.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewCommentResponse {

    private String flag;

    private long commentId;
}
