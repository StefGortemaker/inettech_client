import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        new Client().run();
    }

    private void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 1337);

            if(socket.isConnected()){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println(bufferedReader.readLine());
            } else {
                System.out.println("Could not connect to server");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
