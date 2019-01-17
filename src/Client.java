import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

  public static void main(String[] args) {
    new Client().run();
  }

  private void run() {
    try {
      Socket socket = new Socket("127.0.0.1", 1337);

      if (socket.isConnected()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        Thread serverReader = new Thread(new ServerReader(socket));
        serverReader.start();

        while (true) {
          Scanner scanner = new Scanner(System.in);
          String line = scanner.nextLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
