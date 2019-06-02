package meeting.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OfferListResponse {

    private String flag;

    private List<OfferListItemResponse> offers;

    private List<CommentResponse> comments;

    private List<VoteResponse> votes;
}
