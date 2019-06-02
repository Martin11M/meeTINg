package meeting.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OfferListItemResponse {

    private long id;

    private String startDate;

    private int votesCount;

    private boolean acceptedOffer;

    private boolean confirmedOffer;
}
