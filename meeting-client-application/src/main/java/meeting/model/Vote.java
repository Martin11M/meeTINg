package meeting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Vote {

    private long id;

    private long offerId;

    private long userId;
}
