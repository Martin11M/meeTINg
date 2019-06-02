package meeting.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDataRequest {

    private String flag;

    private String username;

    private String password;

    private boolean isLeader;
}
