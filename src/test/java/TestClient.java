import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestClient {


    private static FJSCAPIClient client = null;
    private static BufferedReader br = null;

    public static void main(String[] args) {
        try {
            client = new FJSCAPIClient("localhost", 24040, "fjscapi", "AutumnSpark1227");
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                receive();
            }
        }).start();
        while (client.connected) {
            try {
                String input = br.readLine();
                if (input.equals("/exit")) {
                    break;
                } else {
                    FJSCAPITransmission transmission = new FJSCAPITransmission(FJSCAPITransferType.TEXT, input, client.getUsername());
                    client.send(transmission);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        try {
            client.disconnect();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private static void receive() {
        while (client.connected) {
            FJSCAPITransmission transmission = null;
            try {
                transmission = client.receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert transmission != null;
            if (transmission.getType() == FJSCAPITransferType.TEXT) {
                System.out.println(transmission.getUser() + ": " + transmission.getContent());
            } else if (transmission.getType() == FJSCAPITransferType.COMMAND && transmission.getUser().equals("server") && transmission.getContent().equals("serverShutdown")) {
                System.out.println("Shutdown");
                System.exit(0);
            } else if (transmission.getType() == FJSCAPITransferType.COMMAND && transmission.getUser().equals("server") && transmission.getContent().equals("clientKicked")) {
                System.out.println("You have been kicked");
                System.exit(0);
            }
        }
    }
}
