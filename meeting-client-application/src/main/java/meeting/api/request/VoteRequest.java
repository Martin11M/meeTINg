package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoteRequest {

    private String flag;

    private long offerId;

    private long userId;
}
