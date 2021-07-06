import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author AutumnSpark1226
 * @version 2021.6.10
 */

public class FJSCAPIClientData {
    private final String username;
    private final int sessionId;
    private String cryptoCode;
    private final Socket socket;
    private final PrintWriter os;
    private final BufferedReader is;
    public Boolean connected;

    public FJSCAPIClientData(Socket socket, BufferedReader is, PrintWriter os, String username, String cryptoCode, Integer sessionId) {
        this.socket = socket;
        this.is = is;
        this.os = os;
        this.username = username;
        this.cryptoCode = cryptoCode;
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

    public String getCryptoCode() {
        return this.cryptoCode;
    }

    public Integer getSessionId() {
        return this.sessionId;
    }

    public void setCryptoCode(String newCryptoCode) {
        this.cryptoCode = newCryptoCode;
    }
}
