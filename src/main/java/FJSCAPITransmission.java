import java.time.Instant;
import java.util.Random;

/**
 * @author AutumnSpark1226
 * @version 2021.6.10
 */

public class FJSCAPITransmission {
    private FJSCAPITransferType type;
    private long id;
    private String content;
    private String user;
    private Instant creationTime;

    public FJSCAPITransmission(FJSCAPITransferType type, String content) {
        Random ran = new Random();
        this.type = type;
        this.content = content;
        this.id = ran.nextLong();
        this.creationTime = Instant.now();
    }

    public FJSCAPITransmission(FJSCAPITransferType type, String content, String user) {
        Random ran = new Random();
        this.type = type;
        this.content = content;
        this.id = ran.nextLong();
        this.user = user;
        this.creationTime = Instant.now();
    }

    public FJSCAPITransmission(FJSCAPITransferType type, String content, String user, long id, Instant creationTime) {
        this.type = type;
        this.content = content;
        this.id = id;
        this.user = user;
        this.creationTime = creationTime;
    }

    public FJSCAPITransmission setType(FJSCAPITransferType type) {
        this.type = type;
        return this;
    }

    public FJSCAPITransmission setId(long id) {
        this.id = id;
        return this;
    }

    public FJSCAPITransmission setContent(String content) {
        this.content = content;
        return this;
    }

    public FJSCAPITransmission setUser(String user) {
        this.user = user;
        return this;
    }

    public long getId() {
        return this.id;
    }

    public FJSCAPITransferType getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    public String getUser() {
        return this.user;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }
}
