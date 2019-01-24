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

                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();

                writer.println("HELO " + line);
                writer.flush();

                while (true) {
                    line = scanner.nextLine();
                    if (line.contains("/bsct")){
                        String splitMessage[] = line.split(" ", 2);
                        String message = "BSCT " + splitMessage[1];
                        writer.println(message);
                        writer.flush();
                    } else if (line.contains("/pm")) {
                        String splitMessage[] = line.split(" ", 3);
                        String message = "PM " + splitMessage[1] + splitMessage[2];
                        writer.println(message);
                        writer.flush();
                    } else if (line.contains("/help")){
                        System.out.println("Commands:");
                        System.out.println("/bsct <Message> (broadcast message)");
                        System.out.println("/pm <Username> <Message> (send pm)");
                        System.out.println("/grpc <Groupname> (create a group)");
                        System.out.println("/grpj <Groupname> (join a group)");
                        System.out.println("/grps <Groupname> <Message> (send message in a group)");
                        System.out.println("/grpl <Groupname> (leave group)");
                        System.out.println("/grpk <Groupname> <Username> (kick user from group)");
                        System.out.println("/quit (quit)");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
