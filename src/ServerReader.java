import java.io.*;
import java.net.Socket;

/**
 * The ServerReader class reads the incoming messages from the server and prints them for the user to read.
 */

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

    /**
     * The fileReceived method prints that a file was successfully received by a user
     *
     * @param message A message containing the filename and the receiving user's name
     */
    private void fileReceived(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " successfully received file: " + splitMessage[1]);
    }

    /**
     * The printBroadcastMessage method prints out broadcast messages
     *
     * @param message A message containing the name of the user that send the message and the message
     */
    private void printBroadcastMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        message = splitMessage[0] + ": " + splitMessage[1];
        System.out.println(message);
    }

    /**
     * The printClientList method prints the incoming list of clients
     *
     * @param message A message containing a list of user names
     */
    private void printClientList(String message) {
        System.out.println("Client List:");
        System.out.println(message);
    }

    /**
     * The printDirectMessage method prints direct messages
     *
     * @param message A message containing the name of the user that send the message and the message
     */
    private void printDirectMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        message = "-> " + splitMessage[0] + ": " + splitMessage[1];
        System.out.println(message);
    }

    /**
     * The printError method prints error messages the user receives from the server
     *
     * @param message A message containing the error message
     */
    private void printError(String message) {
        String colorCode = "\u001b[31m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append("ERR: ").append(message);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    /**
     * The printFileTransferAccepted method prints out that a user has accepted the file transfer. This method also
     * sends a TRANSFER_FILE message to the accepting user.
     *
     * @param message A message containing the accepting user's name and the filename
     * @throws IOException throws an exception when there goes something wrong with reading or writing the file
     */
    private void printFileTransferAccepted(String message) throws IOException {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " accepted file transfer request for file: " + splitMessage[1]);
        File fileToSend = client.getTransferableFiles().get(splitMessage[0]);
        client.sendClientMessage(splitMessage[0] + " " + splitMessage[1], ClientMessage.MessageType.TRANSFER_FILE);
        client.sendFile(fileToSend);
    }

    /**
     * The printFileTransferRequest prints out a file transfer request
     *
     * @param message A message containing the sending user's name and the filename
     */
    private void printFileTransferRequest(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("Would you like to receive file: " + splitMessage[1] + " from " + splitMessage[0] + "[Y/N]");
        client.getIncomingTransferFileRequests().put(splitMessage[0], splitMessage[1]);
        client.setFileTransferRequest(true);
    }

    /**
     * The printFileTransferAccepted method prints out that a user has denied the file transfer.
     *
     * @param message A message containing the denying user's name and the filename
     */
    private void printFileTransferDenied(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println(splitMessage[0] + " denied file transfer request for file: " + splitMessage[1]);
        client.getTransferableFiles().remove(splitMessage[0]);
    }

    /**
     * The printGroupList method prints the incoming list of clients
     *
     * @param message A message containing a list of groups
     */
    private void printGroupList(String message) {
        System.out.println("Group List:");
        System.out.println(message);
    }

    /**
     * The printDirectMessage method prints group messages
     *
     * @param message A message containing the name of the user that send the message, the message and the group the
     *                message was sent in
     */
    private void printGroupMessage(String message) {
        String[] splitMessage = message.split(" ", 3);
        System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + ": " + splitMessage[2]);
    }

    /**
     * The printUserJoinedGroup method print out that a user has joined a certain group
     *
     * @param message A message containing the group name and the name of the user that joined the group
     */
    private void printUserJoinedGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("=> " + splitMessage[1] + " -> " + splitMessage[0] + " joined group");
    }

    /**
     * The printUserKickedGroup method print out that a user has been kicked from a certain group
     *
     * @param message A message containing the group name and the name of the user that got kicked from the group
     */
    private void printUserKickedFromGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        if (splitMessage.length == 2)
            System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + " kicked from group");
        else
            System.out.println("=> " + message + " -> Kicked from group");
    }

    /**
     * The printUserLeftGroup method print out that a user has left a certain group
     *
     * @param message A message containing the group name and the name of the user that left the group
     */
    private void printUserLeftGroup(String message) {
        String[] splitMessage = message.split(" ", 2);
        System.out.println("=> " + splitMessage[0] + " -> " + splitMessage[1] + "left the group");
    }

    /**
     * The printIncomingMessage method prints out the incoming server messages in red
     *
     * @param incomingMessage The ServerMessage that has been received
     */
    private void printIncomingMessage(String incomingMessage){
        String colorCode = "\u001b[31m";
        StringBuilder outgoingMessage = (new StringBuilder()).append(colorCode).append(">> ").append(incomingMessage);
        System.out.println(outgoingMessage.append("\u001b[0m").toString());
    }

    /**
     * The receiveFile will read the incoming file from the DataInputStream and write it to a file through the
     * FileOutputStream. First it will create a DataInputStream from where the file is read. Then it will read the
     * filename and create a FileOutputStream using that filename. After it will read the size of the file and create
     * a buffer based on the size. After the buffer is created it will read the file whilst writing the file. When it's
     * done reading and writing the file it will send a last message to the sender of the file that the file was
     * received.
     *
     * @param message A message containing the filename and the user that send the file
     * @throws IOException throws an exception when something goes on whilst reading or writing the file
     */
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

    /**
     * The sendPong method sends a pong message to the server
     */
    private void sendPong() {
        client.sendClientMessage("", ClientMessage.MessageType.PONG);
    }

    void stop() throws IOException {
        isRunning = false;
        socket.close();
    }
}
