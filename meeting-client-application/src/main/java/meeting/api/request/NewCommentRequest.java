package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewCommentRequest {

    private String flag;

    private long userId;

    private long eventId;

    private String message;

    private String postDate;
}
