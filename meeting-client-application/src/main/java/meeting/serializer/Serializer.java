package meeting.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meeting.api.Client;
import meeting.api.request.*;
import meeting.api.response.*;

public class Serializer {

    private Gson gson;
    private Client client;

    public Serializer(Client client) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
        this.client = client;
    }

    public UserLoginResponse signIn(UserDataRequest userDataRequest) {
        String request = gson.toJson(userDataRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, UserLoginResponse.class);
    }

    public FlagResponse register(UserDataRequest userDataRequest) {
        String request = gson.toJson(userDataRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public NewGroupResponse createGroup(NewGroupRequest newGroupRequest) {
        String request = gson.toJson(newGroupRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, NewGroupResponse.class);
    }

    public GroupListResponse refreshGroups(GroupListRequest groupRequest) {
        String request = gson.toJson(groupRequest);
        String groupResponseString = client.sendRequestRecResponse(request);
        return gson.fromJson(groupResponseString, GroupListResponse.class);
    }

    public FlagResponse applyForMembership(MembershipRequest membershipRequest) {
        String request = gson.toJson(membershipRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public GroupListResponse refreshAllGroups(GroupListRequest allGroupsRequest) {
        String request = gson.toJson(allGroupsRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, GroupListResponse.class);
    }

    public FlagResponse makeRequestDecision(RequestDecisionRequest requestDecisionRequest) {
        String request = gson.toJson(requestDecisionRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public RequestReviewListResponse refreshRequests(RequestReviewListRequest requestReviewListRequest) {
        String request = gson.toJson(requestReviewListRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, RequestReviewListResponse.class);
    }

    public NewEventResponse createEvent(NewEventRequest newEventRequest) {
        String request = gson.toJson(newEventRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, NewEventResponse.class);
    }

    public EventListResponse refreshEvents(EventListRequest eventListRequest) {
        String request = gson.toJson(eventListRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, EventListResponse.class);
    }

    public OfferResponse createOffer(OfferRequest offerRequest) {
        String request = gson.toJson(offerRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, OfferResponse.class);
    }

    public OfferListResponse refreshOffers(OfferListRequest groupRequest) {
        String request = gson.toJson(groupRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, OfferListResponse.class);
    }

    public FlagResponse acceptOffer(OfferAcceptRequest offerAcceptRequest) {
        String request = gson.toJson(offerAcceptRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public FlagResponse confirmOffer(OfferConfirmRequest offerConfirmRequest) {
        String request = gson.toJson(offerConfirmRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public FlagResponse createVote(VoteRequest voteRequest) {
        String request = gson.toJson(voteRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, FlagResponse.class);
    }

    public NewCommentResponse createComment(NewCommentRequest newCommentRequest) {
        String request = gson.toJson(newCommentRequest);
        String response = client.sendRequestRecResponse(request);
        return gson.fromJson(response, NewCommentResponse.class);
    }
}
