package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferListRequest {

    private String flag;

    private long eventId;
}
