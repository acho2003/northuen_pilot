package bt.northuen.api.dto;

public record IncomingPickDropCallResponse(
        PickDropCallSessionResponse call,
        PickDropOrderResponse order
) {
}
