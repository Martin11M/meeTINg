package meeting.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestReview {

    private long groupId;

    private String groupName;

    private String userName;

    private long userId;
}
