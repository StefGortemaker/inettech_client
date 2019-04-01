import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        new Client().run();
    }

    private boolean isRunning = true;
    private PrintWriter writer;

    private void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 1337);

            if (socket.isConnected()) {
                writer = new PrintWriter(socket.getOutputStream());
                Thread serverReader = new Thread(new ServerReader(socket));
                serverReader.start();

                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();
                sendClientMessage(line, ClientMessage.MessageType.HELO);

                while (isRunning) {
                    line = scanner.nextLine();
                    String[] splitLine = line.split(" ", 2);
                    switch (splitLine[0]) {
                        case "/bcst":
                            if (splitLine.length != 2) EmptyMessageError("/bcst");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.BCST);
                            break;
                        case "/clst":
                            sendClientMessage("", ClientMessage.MessageType.CLTLIST);
                            break;
                        case "/pm":
                            if (splitLine.length != 2) EmptyMessageError("/pm");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.PM);
                            break;
                        case "/glst":
                            sendClientMessage("", ClientMessage.MessageType.GRP_LIST);
                            break;
                        case "/grpc":
                            if (splitLine.length != 2) EmptyMessageError("/grpc");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.GRP_CREATE);
                            break;
                        case "/grpj":
                            if (splitLine.length != 2) EmptyMessageError("/grpj");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.GRP_JOIN);
                            break;
                        case "/grps":
                            if (splitLine.length != 2) EmptyMessageError("/grpl");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.GRP_SEND);
                            break;
                        case "/grpl":
                            if (splitLine.length != 2) EmptyMessageError("/bcst");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.GRP_LEAVE);
                            break;
                        case "/grpk":
                            if (splitLine.length != 2) EmptyMessageError("/grpk");
                            else sendClientMessage(splitLine[1], ClientMessage.MessageType.GRP_KICK);
                            break;
                        case "/help":
                            printHelp();
                            break;
                        case "/quit":
                            writerPrint(ClientMessage.MessageType.QUIT.toString());
                            break;
                        default:
                            System.out.println("Error: \"" + splitLine[0] + "\" is an invalid command, " +
                                    "try \"/help\" for a list of commands");
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The EmptyMessageError method prints an error when a command that needs content doesn't contain content
     *
     * @param command The command that the user is trying to execute
     */
    private void EmptyMessageError(String command) {
        System.out.println("Error: \"" + command + "\" doesn't contain the right content, " +
                "try \"/help\" for a list of commands");
    }

    /**
     * The printHelp function prints all commands that are available, how to use them and what they do.
     */
    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("/bcst <Message> (broadcast message)");
        System.out.println("/clst (shows list of online users)");
        System.out.println("/pm <Username> <Message> (send pm)");
        System.out.println("/glst (shows list of groups)");
        System.out.println("/grpc <Group name> (create a group)");
        System.out.println("/grpj <Group name> (join a group)");
        System.out.println("/grps <Group name> <Message> (send message in a group)");
        System.out.println("/grpl <Group name> (leave group)");
        System.out.println("/grpk <Group name> <Username> (kick user from group)");
        System.out.println("/quit (quit)");
    }

    /**
     * The printOutgoingMessages method prints the message that is send to the server in the color green, so that the
     * user can see what commands the client sends to the server.
     *
     * @param message The message that is send to the server.
     */
    private void printOutgoingMessages(String message) {
        String colorCode = "\u001b[32m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append("<< ").append(message);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    /**
     * The sendClientMessage converts the message and its type into a message that the server can read. It takes the
     * message itself and the type of the message. First it checks the type of message and then converts the message
     * correspondingly.
     *
     * @param message The message that needs to be send to the server
     * @param type    The type of the message
     */
    private void sendClientMessage(String message, ClientMessage.MessageType type) {
        ClientMessage clientMessage = new ClientMessage(type, message);
        printOutgoingMessages(clientMessage.toString());
        writerPrint(clientMessage.toString());
    }

    /**
     * The writerPrint method user the printWriter to send a message to the server.
     *
     * @param message The message that needs to be send to the server
     */
    private void writerPrint(String message) {
        writer.println(message);
        writer.flush();
    }
}
