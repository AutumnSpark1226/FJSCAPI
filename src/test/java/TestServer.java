import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestServer {
    public static FJSCAPIServer server;

    public static void main(String[] args) {
        try {
            server = new FJSCAPIServer(24040, 10, "fjscapi", "echoServer");
            server.addListener(clientData -> {
                while (clientData.connected) {
                    FJSCAPITransmission transmission = null;
                    try {
                        transmission = server.receive(clientData);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    if (server.getKickedSessionId().contains(clientData.getSessionId())) {
                        clientData.connected = false;
                        break;
                    }
                    assert transmission != null;
                    if (transmission.getType() == FJSCAPITransferType.COMMAND) {
                        if (transmission.getContent().equals("disconnect")) {
                            System.out.println(clientData.getUsername() + " disconnected");
                            break;
                        }
                    } else if (transmission.getType() == FJSCAPITransferType.TEXT) {
                        try {
                            System.out.println(transmission.getContent());
                            server.send(clientData, transmission);
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
                try {
                    clientData.getIs().close();
                    clientData.getOs().close();
                    clientData.getSocket().close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command = null;
        while (true) {
            /*try {
                command = br.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (command.equals("/shutdown")) {
                try {
                    FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.COMMAND, "serverShutdown", "server");
                    server.sendToAnyone(transmission);
                    server.close();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (command.startsWith("/kick")) {
                String kickedUsername = command.replace("/kick ", "");
                try {
                    server.kick(kickedUsername);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
            try {
                server.acceptClients();
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
