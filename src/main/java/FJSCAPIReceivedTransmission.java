import java.time.Instant;

public class FJSCAPIReceivedTransmission extends FJSCAPITransmission{

    private final FJSCAPIClient client;

    public FJSCAPIReceivedTransmission(FJSCAPITransferType type, String content, String user, long id, Instant creationTime, FJSCAPIClient client) {
        super(type, content, user, id, creationTime);
        this.client = client;
    }

    public FJSCAPIReceivedTransmission(FJSCAPITransferType type, String content, String user, FJSCAPIClient client) {
        super(type, content, user);
        this.client = client;
    }
}
