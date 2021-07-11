import Exception.FJSCAPIError;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the server.
 * @author AutumnSpark1226
 * @version 2021.7.11
 */

public class FJSCAPIServer {
    private final ServerSocket server;
    private final int port;
    private String password;
    private final String serverType;
    private final ArrayList<FJSCAPIClientData> clients;
    private final ArrayList<String> usernames;
    private final int serverSlots;
    private final ArrayList<Integer> kickedSessionId;
    private final List<FJSCAPIClientConnectedListener> listeners = new ArrayList<>();
    private Boolean filesAccepted = false;
    private String directory = "";
    private long maxFileSize = 0;
    private int nextSessionId = 1;
    private final Thread pingThread;

    public FJSCAPIServer(int newPort, int slots, String newPassword, String newServerType) throws Exception {
        this.port = newPort;
        this.serverSlots = slots;
        this.password = FJSCAPICrypto.hash3_256(newPassword);
        this.serverType = newServerType;
        this.server = new ServerSocket(port);
        this.clients = new ArrayList<>(serverSlots);
        this.usernames = new ArrayList<>(serverSlots);
        this.kickedSessionId = new ArrayList<>(serverSlots);
        this.pingThread = new Thread(() -> {
            while (true) {
                ping();
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }

    private void initialiseClient(Socket socket) throws Exception {
        BufferedReader is;
        PrintWriter os;
        String username = null;
        String cryptoCode = FJSCAPICrypto.generateString();
        int sessionId = nextSessionId;
        nextSessionId++;
        Boolean ok = true;
        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        os = new PrintWriter(socket.getOutputStream(), true);
        String receivedPw = receivePassword(socket, cryptoCode, is);
        if (receivedPw.equals(this.password)) {
            os.println(FJSCAPICrypto.encrypt(cryptoCode, "pwCorrect"));
            os.println(FJSCAPICrypto.encrypt(cryptoCode, this.serverType));
            username = FJSCAPICrypto.decrypt(cryptoCode, is.readLine());
            os.println(FJSCAPICrypto.encrypt(cryptoCode, String.valueOf(sessionId)));
            if (username.length() <= 3 && username.equalsIgnoreCase("server")) { //This should always be false, but there should be no chance for clients without this test.
                os.println(FJSCAPICrypto.encrypt(cryptoCode, "usernameError"));
            } else if (this.usernames.size() >= this.serverSlots || this.clients.size() >= this.serverSlots) {
                os.println(FJSCAPICrypto.encrypt(cryptoCode, "serverFull"));
                if (this.usernames.size() != this.clients.size()) {
                    FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.COMMAND, "serverError", "server");
                    sendToAnyone(transmission);
                }
                ok = false;
                os.println(FJSCAPICrypto.encrypt(cryptoCode, "serverFull"));
            } else if (this.usernames.contains(username)) {
                ok = false;
                os.println(FJSCAPICrypto.encrypt(cryptoCode, "usernameAlreadyExists"));
            } else {
                this.usernames.add(username);
            }
        } else {
            ok = false;
            os.println(FJSCAPICrypto.encrypt(cryptoCode, "pwError"));
            is.close();
            os.close();
            socket.close();
        }
        if (ok) {
            try {
                os.println(FJSCAPICrypto.encrypt(cryptoCode, "loginOk"));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            FJSCAPIClientData clientData = new FJSCAPIClientData(socket, is, os, username, cryptoCode, sessionId);
            this.clients.add(clientData);
            for (FJSCAPIClientConnectedListener hl : this.listeners) {
                Thread thread = new Thread(() -> hl.connected(clientData));
                thread.start();
            }
        }

    }

    public void sendToAnyone(FJSCAPITransmission transmission) throws Exception {
        for (FJSCAPIClientData client : this.clients) {
            send(client, transmission);
        }
    }

    public void send(FJSCAPIClientData clientData, FJSCAPITransmission transmission) throws Exception {
        clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), String.valueOf(transmission.getType())));
        clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), String.valueOf(transmission.getId())));
        clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), transmission.getContent()));
        clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), transmission.getUser()));
        clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), transmission.getCreationTime().toString()));
    }

    public FJSCAPITransmission receive(FJSCAPIClientData clientData) throws Exception {
        if (this.kickedSessionId.contains(clientData.getSessionId())) {
            this.kickedSessionId.remove(clientData.getSessionId());
            throw new FJSCAPIError(clientData.getUsername() + " was kicked");
        }
        FJSCAPITransmission transmission;
        FJSCAPITransferType type = FJSCAPITransferType.valueOf(FJSCAPICrypto.decrypt(clientData.getCryptoCode(), clientData.getIs().readLine()));
        long id = Long.parseLong(FJSCAPICrypto.decrypt(clientData.getCryptoCode(), clientData.getIs().readLine()));
        String content = FJSCAPICrypto.decrypt(clientData.getCryptoCode(), clientData.getIs().readLine());
        Instant creationTime = Instant.parse(FJSCAPICrypto.decrypt(clientData.getCryptoCode(), clientData.getIs().readLine()));
        transmission = new FJSCAPITransmission(type, content, clientData.getUsername(), id, creationTime);
        if (transmission.getType() == FJSCAPITransferType.COMMAND && transmission.getContent().equals("disconnect")) {
            this.clients.remove(clientData);
            this.usernames.remove(clientData.getUsername());
        }
        if (this.filesAccepted && transmission.getType() == FJSCAPITransferType.FILE) { // TODO doesn't work well
            /*String fileName = transmission.getContent();
            System.out.println("name: " + fileName);
            long fileSize = Long.parseLong(FJSCAPICrypto.decrypt(clientData.getCryptoCode(), clientData.getIs().readLine()));
            System.out.println("size: " + fileSize);
            //String fileCryptoCode = FJSCAPICrypto.generateString();
            //clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), fileCryptoCode));
            if (fileSize >= maxFileSize) {
                transmission.setContent("fileTooBig");
                clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), "fileTooBig"));
            } else {
                long xFileSize = fileSize;
                String filePath = this.directory + /*"/" + clientData.getUsername() + * /"/" + fileName;
                DataInputStream dataInputStream = new DataInputStream(clientData.getSocket().getInputStream());
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                byte[] buffer = new byte[(int) Math.min(4112, (fileSize / 16 + 1) * 16)];
                String uploadCode = FJSCAPICrypto.generateString();
                clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), uploadCode));
                clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), "uploadStart"));
                while (fileSize > 0 && dataInputStream.read(buffer, 0, buffer.length) != -1) {
                    //System.out.println(buffer.length);
                    //System.out.println(Arrays.toString(buffer));
                    //buffer = FJSCAPICrypto.decryptByte(uploadCode, buffer);
                    //System.out.println(buffer.length);
                    //System.out.println(Arrays.toString(buffer));
                    fileOutputStream.write(buffer, 0, buffer.length);
                    fileSize -= buffer.length;
                    if (fileSize < 1) {
                        break;
                    }
                    buffer = new byte[(int) Math.min(4112, (fileSize / 16 + 1) * 16)];
                }
                fileOutputStream.close();
                File file = new File(filePath);
                clientData.getOs().println(FJSCAPICrypto.encrypt(clientData.getCryptoCode(), String.valueOf(xFileSize == file.length())));
                if (xFileSize != file.length()) {
                    transmission.setContent("fileCorrupted");
                }
            }
             */
        }
        return transmission;
    }

    public Boolean kick(String kickedUsername) throws Exception {
        Boolean output = false;
        int iClients = this.clients.size();
        for (int i = 0; i < iClients; i++) {
            if (this.clients.get(i).getUsername().equals(kickedUsername) || kickedUsername.equals(clients.get(i).getSessionId().toString()) || kickedUsername.equals("@all")) {
                this.kickedSessionId.add(this.clients.get(i).getSessionId());
                FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.COMMAND, "clientKicked", "server");
                send(this.clients.get(i), transmission);
                this.usernames.remove(this.clients.get(i).getUsername());
                this.clients.remove(this.clients.get(i));
                output = true;
            }
        }
        return output;
    }

    public void addListener(FJSCAPIClientConnectedListener toAdd) {
        this.listeners.add(toAdd);
    }

    public int getPort() {
        return this.port;
    }

    public String getServerType() {
        return this.serverType;
    }

    public ArrayList<FJSCAPIClientData> getClients() {
        return this.clients;
    }

    public int getServerSlots() {
        return this.serverSlots;
    }

    public ArrayList<String> getUsernames() {
        return this.usernames;
    }

    public ArrayList<Integer> getKickedSessionId() {
        return this.kickedSessionId;
    }

    public List<FJSCAPIClientConnectedListener> getListeners() {
        return this.listeners;
    }

    public void close() throws Exception {
        pingThread.interrupt();
        FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.COMMAND, "serverShutdown", "server");
        this.sendToAnyone(transmission);
        this.server.close();
    }

    public void activateFileTransfer(String newDirectory, long newMaxFileSize) throws Exception {
        File folder = new File(newDirectory);
        if (folder.isDirectory() && folder.canWrite() && folder.canRead() && newMaxFileSize > 0) {
            this.filesAccepted = true;
            this.directory = newDirectory;
            this.maxFileSize = newMaxFileSize;
        } else {
            this.filesAccepted = false;
            throw new FJSCAPIError(newDirectory + " is not a directory, is read-only or the file size isn't bigger than 0");
        }
    }

    public Boolean areFilesAccepted() {
        return this.filesAccepted;
    }

    public void disableFileTransfer() {
        this.filesAccepted = false;
    }

    public void setMaxFileSize(long newMaxFileSize) throws Exception {
        if (newMaxFileSize > 0) {
            this.maxFileSize = newMaxFileSize;
        } else {
            throw new FJSCAPIError("The size must be bigger than 0");
        }
    }

    public void acceptClients() throws Exception {
        initialiseClient(this.server.accept());
    }

    private void ping() {
        try {
            for (FJSCAPIClientData client : this.clients) {
                try {
                    InetAddress address = client.getSocket().getInetAddress();
                    if (!address.isReachable(5000)) {
                        this.kick(client.getUsername());
                    }
                } catch (Exception e) {
                    this.kick(client.getUsername());
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void setPassword(String hashedPassword) {
        /**
         * The input must be encrypted with SHA3-256.
         */
        this.password = hashedPassword;
    }

    public void setPlainPassword(String hashedPassword) throws Exception {
        /**
         * The input must not be encrypted.
         */
        this.password = FJSCAPICrypto.hash3_256(hashedPassword);
    }

    private String receivePassword(Socket socket, String cryptoCode, BufferedReader is) throws Exception{
        byte[] lenb = new byte[4];
        socket.getInputStream().read(lenb, 0, 4);
        ByteBuffer bb = ByteBuffer.wrap(lenb);
        int len = bb.getInt();
        byte[] servPubKeyBytes = new byte[len];
        socket.getInputStream().read(servPubKeyBytes);
        PublicKey publicKey = FJSCAPICrypto.recreatePublicKey(servPubKeyBytes);
        byte[] bytes = FJSCAPICrypto.encryptWithPublicKey(publicKey, cryptoCode.getBytes());
        bb = ByteBuffer.allocate(4);
        bb.putInt(bytes.length);
        socket.getOutputStream().write(bb.array());
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
        return FJSCAPICrypto.decrypt(cryptoCode, is.readLine());
    }
}
