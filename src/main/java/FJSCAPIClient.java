import Exception.FJSCAPILoginException;
import Exception.FJSCAPIPasswordException;
import Exception.FJSCAPIUsernameException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.time.Instant;

/**
 * @author AutumnSpark1226
 * @version 2021.6.2
 */

public class FJSCAPIClient {
    public Boolean connected;
    private final String username;
    private final int sessionId;
    private final String cryptoCode;
    private final Socket socket;
    private final PrintWriter os;
    private final BufferedReader is;
    private final String serverType;

    public FJSCAPIClient(String host, int port, String password, String username) throws Exception {
        /**
         * The constructor needs host, port, password and username and connects to the server. The username must be longer than 3 letters.
         */
        if (username.length() > 3 && !username.equalsIgnoreCase("server")) {
            this.username = username;
            this.socket = new Socket(host, port);
            this.os = new PrintWriter(this.socket.getOutputStream(), true);
            this.is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            KeyPair keyPair = FJSCAPICrypto.generateKeyPair(512);
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(keyPair.getPublic().getEncoded().length);
            socket.getOutputStream().write(bb.array());
            socket.getOutputStream().write(keyPair.getPublic().getEncoded());
            socket.getOutputStream().flush();
            byte[] lenb = new byte[4];
            socket.getInputStream().read(lenb, 0, 4);
            bb = ByteBuffer.wrap(lenb);
            int len = bb.getInt();
            byte[] bytes = new byte[len];
            socket.getInputStream().read(bytes);
            this.cryptoCode = new String(FJSCAPICrypto.decryptWithPrivateKey(keyPair.getPrivate(), bytes));
            this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, FJSCAPICrypto.hash3_256(password)));
            String input = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
            if (input.equals("pwCorrect")) {
                this.serverType = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, this.username));
                this.sessionId = Integer.parseInt(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
                String serverResponse = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
                if (serverResponse.equals("loginOk")) {
                    this.connected = true;
                } else {
                    this.is.close();
                    this.os.close();
                    this.socket.close();
                    throw new FJSCAPILoginException(serverResponse);
                }
            } else {
                this.is.close();
                this.os.close();
                this.socket.close();
                throw new FJSCAPIPasswordException("Wrong password");
            }
        } else {
            if (username.equalsIgnoreCase("server")) {
                throw new FJSCAPIUsernameException("Your username mustn't be 'server'");
            } else {
                throw new FJSCAPIUsernameException("Your username is too short.");
            }

        }
        System.gc();
    }

    public FJSCAPIClient(String host, int port, String password, Boolean passwordHashed, String username) throws Exception {
        /**
         * The constructor needs host, port, password and username and connects to the server. The username must be longer than 3 letters.
         * If you want to use a hashed password set password hashed to true. The password must be hashed with SHA3-256.
         */
        if (username.length() > 3 && !username.equalsIgnoreCase("server")) {
            this.username = username;
            if (!passwordHashed) {
                password = FJSCAPICrypto.hash3_256(password);
            }
            this.socket = new Socket(host, port);
            this.os = new PrintWriter(this.socket.getOutputStream(), true);
            this.is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            KeyPair keyPair = FJSCAPICrypto.generateKeyPair(512);
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(keyPair.getPublic().getEncoded().length);
            socket.getOutputStream().write(bb.array());
            socket.getOutputStream().write(keyPair.getPublic().getEncoded());
            socket.getOutputStream().flush();
            byte[] lenb = new byte[4];
            socket.getInputStream().read(lenb, 0, 4);
            bb = ByteBuffer.wrap(lenb);
            int len = bb.getInt();
            byte[] bytes = new byte[len];
            socket.getInputStream().read(bytes);
            this.cryptoCode = new String(FJSCAPICrypto.decryptWithPrivateKey(keyPair.getPrivate(), bytes));
            this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, password));
            String input = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
            if (input.equals("pwCorrect")) {
                this.serverType = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, this.username));
                this.sessionId = Integer.parseInt(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
                String serverResponse = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
                if (serverResponse.equals("loginOk")) {
                    this.connected = true;
                } else {
                    this.is.close();
                    this.os.close();
                    this.socket.close();
                    throw new FJSCAPILoginException(serverResponse);
                }
            } else {
                this.is.close();
                this.os.close();
                this.socket.close();
                throw new FJSCAPIPasswordException("Wrong password");
            }
        } else {
            if (username.equalsIgnoreCase("server")) {
                throw new FJSCAPIUsernameException("Your username mustn't be 'server'");
            } else {
                throw new FJSCAPIUsernameException("Your username is too short.");
            }

        }
        System.gc();
    }

    public String getServerType() {
        /**
         * The method returns the type of the server.
         */
        return this.serverType;
    }

    public void send(FJSCAPITransmission transmission) throws Exception {
        if (transmission.getContent() != null && transmission.getContent().length() != 0) {
            if (transmission.getType() == FJSCAPITransferType.FILE) { // TODO todo
                /*File file = new File(transmission.getContent());
                if (!file.canRead()) {
                    throw new FJSCAPIError("I can't read the file");
                }
                if (!file.isFile()) {
                    throw new FJSCAPIError("This is no file");
                }
                transmission.setContent(file.getName());
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, String.valueOf(transmission.getType())));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, String.valueOf(transmission.getId())));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, file.getName()));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, transmission.getCreationTime().toString()));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, String.valueOf(file.length())));
                DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                FileInputStream fileInputStream = new FileInputStream(file);
                //send file
                long bytesLeft = file.length();
                byte[] buffer = new byte[(int) Math.min(4096, bytesLeft)];
                String uploadCode = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
                if (FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()).equals("uploadStart")) {
                    while (bytesLeft > 0 && fileInputStream.read(buffer, 0, buffer.length) != -1) {
                        //System.out.println(buffer.length);
                        //System.out.println(Arrays.toString(buffer));
                        bytesLeft -= buffer.length;
                        //buffer = FJSCAPICrypto.encryptByte(uploadCode, buffer);
                        //System.out.println(buffer.length);
                        //System.out.println(Arrays.toString(buffer));
                        dataOutputStream.write(buffer, 0, buffer.length);
                        dataOutputStream.flush();
                        buffer = new byte[(int) Math.min(4096, bytesLeft)];
                    }
                    fileInputStream.close();
                    Boolean worked = Boolean.valueOf(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
                    if (!worked) {
                        throw new FJSCAPIError("fileCorrupted");
                    }
                } else {
                    throw new FJSCAPIError("ERROR");
                }

                 */
            } else {
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, String.valueOf(transmission.getType())));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, String.valueOf(transmission.getId())));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, transmission.getContent()));
                this.os.println(FJSCAPICrypto.encrypt(this.cryptoCode, transmission.getCreationTime().toString()));
            }
        }
    }

    public void disconnect() throws Exception {
        FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.COMMAND, "disconnect");
        this.send(transmission);
        this.is.close();
        this.os.close();
        this.socket.close();
        this.connected = false;
    }

    public FJSCAPITransmission receive() throws Exception {
        FJSCAPITransmission transmission;
        FJSCAPITransferType type = FJSCAPITransferType.valueOf(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
        long id = Long.parseLong(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
        String content = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
        String user = FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine());
        Instant creationTime = Instant.parse(FJSCAPICrypto.decrypt(this.cryptoCode, this.is.readLine()));
        transmission = new FJSCAPITransmission(type, content, user, id, creationTime);
        if (type == FJSCAPITransferType.COMMAND) {
            if (content.equals("serverShutdown") && user.equalsIgnoreCase("server")) {
                this.connected = false;
                this.os.close();
                this.is.close();
                this.socket.close();
            }
            if (content.equals("serverError") && user.equalsIgnoreCase("server")) {
                this.connected = false;
                this.os.close();
                this.is.close();
                this.socket.close();
            }
            if (content.equals("clientKicked") && user.equalsIgnoreCase("server")) {
                this.connected = false;
                this.os.close();
                this.is.close();
                this.socket.close();
            }
        }
        return transmission;
    }

    public String getUsername() {
        return this.username;
    }

    public int getSessionId() {
        return sessionId;
    }
}
