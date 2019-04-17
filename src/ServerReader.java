import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerReader implements Runnable {

    private Socket socket;
    private Client client;
    private boolean isRunning = true;

    ServerReader(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (isRunning) {
                ServerMessage incomingMessage = new ServerMessage(reader.readLine());
                printIncomingMessage(incomingMessage.toString());

                ServerMessage.MessageType messageType = incomingMessage.getMessageType();
                String incomingMessagePayload = incomingMessage.getPayload();

                switch (messageType) {
                    case BCST:
                        printBroadcastMessage(incomingMessagePayload);
                        break;
                    case CLTLIST:
                        printClientList(incomingMessagePayload);
                        break;
                    case ERR:
                        printError(incomingMessagePayload);
                        break;
                    case GRPLIST:
                        printGroupList(incomingMessagePayload);
                        break;
                    case GRP_CREATE:
                        printGroupCreated(incomingMessagePayload);
                        break;
                    case GRP_JOIN:
                        printUserJoinedGroup(incomingMessagePayload);
                        break;
                    case GRP_KICK:
                        printUserKickedFromGroup(incomingMessagePayload);
                        break;
                    case GRP_LEAVE:
                        printUserLeftGroup(incomingMessagePayload);
                        break;
                    case GRP_SEND:
                        printGroupMessage(incomingMessagePayload);
                        break;
                    case HELO:
                        break;
                    case OK:
                        break;
                    case PING:
                        sendPong();
                        break;
                    case PM:
                        printDirectMessage(incomingMessagePayload);
                        break;
                    case REQ_FILE:
                        printFileTransferRequest(incomingMessagePayload);
                        break;
                    case UNKNOWN:
                        break;
                    case QUIT:
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printBroadcastMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        message = splitMessage[0] + ": " + splitMessage[1];
        System.out.println(message);
    }

    private void printClientList(String message) {
        System.out.println(message);
    }

    private void printDirectMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        message = "-> " + splitMessage[0] + ": " + splitMessage[1];
        System.out.println(message);
    }

    private void printError(String message) {
        String colorCode = "\u001b[31m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append("ERR: ").append(message);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    private void printFileTransferRequest(String message) {
        System.out.println(message);
    }

    private void printGroupCreated(String message) {
        System.out.println(message);
    }

    private void printGroupList(String message) {
        System.out.println(message);
    }

    private void printGroupMessage(String message) {
        System.out.println(message);
    }

    private void printUserJoinedGroup(String message) {
        System.out.println(message);
    }

    private void printUserKickedFromGroup(String message) {
        System.out.println(message);
    }

    private void printUserLeftGroup(String message) {
        System.out.println(message);
    }

    private void printIncomingMessage(String incomingMessage){
        String colorCode = "\u001b[31m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append(">> ").append(incomingMessage);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    private void sendPong() {
        client.sendClientMessage("", ClientMessage.MessageType.PONG);
    }

    public void stop() throws IOException {
        isRunning = false;
        socket.close();
    }
}
