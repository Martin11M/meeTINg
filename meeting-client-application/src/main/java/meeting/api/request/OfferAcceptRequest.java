package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferAcceptRequest {

    private String flag;

    private long offerId;
}
