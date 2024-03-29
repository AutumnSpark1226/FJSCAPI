import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class stores the values of the client.
 * @author AutumnSpark1226
 * @version 2021.6.10
 */

public class FJSCAPIClientData {
    private final String username;
    private final int sessionId;
    private SecretKey key;
    private final Socket socket;
    private final PrintWriter os;
    private final BufferedReader is;
    public Boolean connected;

    public FJSCAPIClientData(Socket socket, BufferedReader is, PrintWriter os, String username, SecretKey key, Integer sessionId) {
        this.socket = socket;
        this.is = is;
        this.os = os;
        this.username = username;
        this.key = key;
        this.sessionId = sessionId;
        this.connected = true;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public PrintWriter getOs() {
        return this.os;
    }

    public BufferedReader getIs() {
        return this.is;
    }

    public String getUsername() {
        return this.username;
    }

    public SecretKey getKey() {
        return this.key;
    }

    public Integer getSessionId() {
        return this.sessionId;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }
}
