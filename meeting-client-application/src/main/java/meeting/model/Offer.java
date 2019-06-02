package meeting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Offer {

    private long id;

    private LocalDateTime startDate;

    private int votesCount;

    private boolean acceptedOffer;

    private boolean confirmedOffer;
}
