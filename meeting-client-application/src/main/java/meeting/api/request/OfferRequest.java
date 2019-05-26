package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferRequest {

    private String flag;

    private long eventId;

    private long userId;

    private String date;
}
