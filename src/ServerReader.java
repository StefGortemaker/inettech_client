import java.io.*;
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
            Encyptor encyptor = new Encyptor();
            while (isRunning) {
                String encyptedMessage = reader.readLine();
                ServerMessage incomingMessage = new ServerMessage(encyptor.decrypt(encyptedMessage));
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
                        if (incomingMessagePayload.equals("Goodbye")) {
                            System.out.println("GoodBye");
                            client.stopClient();
                        }
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
                    case ACCEPT_FILE:
                        printFileTransferAccepted(incomingMessagePayload);
                        break;
                    case DENY_FILE:
                        printFileTransferDenied(incomingMessagePayload);
                        break;
                    case TRANSFER_FILE:
                        receiveFile(incomingMessagePayload);
                        break;
                    case FILE_RECEIVED:
                        fileReceived(incomingMessagePayload);
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

    private void fileReceived(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " successfully received file: " + splitMessage[1]);
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

    private void printFileTransferAccepted(String message) throws IOException {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " accepted file transfer request for file: " + splitMessage[1]);
        File fileToSend = client.getTransferableFiles().get(splitMessage[0]);
        client.sendClientMessage(splitMessage[0] + " " + splitMessage[1], ClientMessage.MessageType.TRANSFER_FILE);
        client.sendFile(fileToSend);
    }

    private void printFileTransferRequest(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("Would you like to receive file: " + splitMessage[1] + " from " + splitMessage[0] + "[Y/N]");
        client.getIncomingTransferFileRequests().put(splitMessage[0], splitMessage[1]);
        client.setFileTransferRequest(true);
    }

    private void printFileTransferDenied(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " denied file transfer request for file: " + splitMessage[1]);
        client.getTransferableFiles().remove(splitMessage[0]);
    }

    private void printGroupList(String message) {
        System.out.println(message);
    }

    private void printGroupMessage(String message) {
        String[] splitMessage = message.split(" ", 3);
        System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + ": " + splitMessage[2]);
    }

    private void printUserJoinedGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("=> " + splitMessage[1] + " -> " + splitMessage[0] + " joined group");
    }

    private void printUserKickedFromGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        if (splitMessage.length == 2)
            System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + " kicked from group");
        else
            System.out.println("=> " + message + " -> Kicked from group");
    }

    private void printUserLeftGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + "left the group");
    }

    private void printIncomingMessage(String incomingMessage){
        String colorCode = "\u001b[31m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append(">> ").append(incomingMessage);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    private void receiveFile(String message) throws IOException {
        String[] splitMessage = message.split(" ", 2);

        int bytesRead;

        DataInputStream serverData = new DataInputStream(socket.getInputStream());

        String fileName = serverData.readUTF();
        OutputStream output = new FileOutputStream(fileName);
        long size = serverData.readLong();
        byte[] buffer = new byte[(int) size];
        while (size > 0 && (bytesRead = serverData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
        {
            output.write(buffer, 0, bytesRead);
            size -= bytesRead;
        }

        output.flush();

        client.sendClientMessage(splitMessage[0] + " " + splitMessage[1],
                ClientMessage.MessageType.FILE_RECEIVED);
        System.out.println(fileName + " successfully received");
    }

    private void sendPong() {
        client.sendClientMessage("", ClientMessage.MessageType.PONG);
    }

    void stop() throws IOException {
        isRunning = false;
        socket.close();
    }
}
