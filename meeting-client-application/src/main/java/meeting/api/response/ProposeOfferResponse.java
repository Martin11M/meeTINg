package meeting.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProposeOfferResponse {

    private String flag;

    private long offerId;
}
