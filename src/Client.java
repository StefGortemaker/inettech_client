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

    PrintWriter writer;

    private void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 1337);

            if (socket.isConnected()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                Thread serverReader = new Thread(new ServerReader(socket));
                serverReader.start();

                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();

                writerPrint("HELO " + line);

                while (true) {
                    line = scanner.nextLine();
                    if (line.contains("/bsct")) {
                        sendBroadcastMessage(line);
                    } else if (line.contains("/pm")) {
                        sendDirectMessage(line);
                    } else if (line.contains("/grpc")) {
                        //TODO: groupCreate
                        System.out.println("grpc");
                    } else if (line.contains("/grpj")) {
                        //TODO: groupJoin
                        System.out.println("grpj");
                    } else if (line.contains("/grps")) {
                        //TODO: groupSEnd
                        System.out.println("grps");
                    } else if (line.contains("/grpl")) {
                        //TODO: groupLeave
                        System.out.println("grpl");
                    } else if (line.contains("/grpk")) {
                        //TODO: groupKick
                        System.out.println("grpk");
                    } else if (line.equals("/help")) {
                        printHelp();
                    } else if (line.equals("/quit")) {
                        writerPrint("QUIT");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The printHelp function prints all commands that are available, how to use them and what they do.
     */
    private void printHelp() {
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

    private void sendBroadcastMessage(String message) {
        String splitMessage[] = message.split(" ", 2);
        String broadcastMessage = "BSCT " + splitMessage[1];
        writerPrint(broadcastMessage);
    }

    private void sendDirectMessage(String message) {
        String splitMessage[] = message.split(" ", 2);
        String directMessage = "PM " + splitMessage[1];
        writerPrint(directMessage);
    }

    private void writerPrint(String message) {
        writer.println(message);
        writer.flush();
    }
}
