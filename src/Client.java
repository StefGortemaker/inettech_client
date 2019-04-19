import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        new Client().run();
    }

    private boolean isRunning = true;
    private PrintWriter writer;
    private OutputStream os;

    private Encyptor encyptor;

    private void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 1337);

            if (socket.isConnected()) {

                os = socket.getOutputStream();
                writer = new PrintWriter(os);
                Thread serverReader = new Thread(new ServerReader(socket, this));
                serverReader.start();

                encyptor = new Encyptor();

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
                        case "/trnf":
                            File directory;
                            if (splitLine.length != 2) directory = new File(".");
                            else directory = new File(splitLine[1]);
                            getFilesFromDirectory(directory, scanner);
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

    private void getFilesFromDirectory(File directory, Scanner scanner) {
        File[] filesInDirectory = directory.listFiles();
        List<File> fileList = new ArrayList<>();
        if (filesInDirectory != null) {
            for (File f : filesInDirectory) {
                if (f.isFile()) {
                    fileList.add(f);
                }
            }
            File[] files = new File[fileList.size()];
            fileList.toArray(files);
            printFiles(files, scanner);
        } else {
            System.out.println("invalid file diretory");
        }
    }

    /**
     * The printFiles prints all files that are in the directory
     *
     * @param files list of files that needs to be printed
     */
    private void printFiles(File[] files, Scanner scanner) {
        int i = 1;
        System.out.println("which user do you want to send the file?");
        String userName = scanner.nextLine();
        System.out.println("Select a file you want to send: ");
        for (File f : files) {
            if (f.isFile()) {
                System.out.println(i + ") " + f.getName());
                i++;
            }
        }
        selectFile(files, userName);
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
        System.out.println("/trnf <File Directory> (transfer file from directory)");
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

    private void selectFile(File[] filesList, String userName) {
        Scanner scanner = new Scanner(System.in);
        int selectedFile;
        if (scanner.hasNextInt() && (selectedFile = Integer.valueOf(scanner.nextLine())) <= filesList.length) {
            System.out.println("Selected: " + filesList[selectedFile - 1].getName());
            System.out.println("Are you sure you want to send \"" + filesList[selectedFile - 1].getName() + "\" " +
                    " to " + userName + " [Y/N]");
            String confirm = scanner.nextLine();
            if (confirm.equals("Y") || confirm.equals("y")) {
                sendTransferRequest(filesList[selectedFile - 1], userName);
            } else System.out.println("File not send");
        } else {
            printFiles(filesList, scanner);
        }
    }

    /**
     * The sendClientMessage converts the message and its type into a message that the server can read. It takes the
     * message itself and the type of the message. First it checks the type of message and then converts the message
     * correspondingly.
     *
     * @param message The message that needs to be send to the server
     * @param type    The type of the message
     */
    void sendClientMessage(String message, ClientMessage.MessageType type) {
        ClientMessage clientMessage = new ClientMessage(type, message);
        printOutgoingMessages(clientMessage.toString());
        writerPrint(clientMessage.toString());
    }

    private void sendFile(File file) throws IOException {
        byte[] fileBytes = new byte[(int) file.length()];

        //read file in
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        dis.readFully(fileBytes, 0, fileBytes.length);

        //send file out
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());
        dos.write(fileBytes, 0, fileBytes.length);
        dos.flush();
    }

    private void sendTransferRequest(File file, String userName) {
        System.out.println("sendTransferRequest");
        String message = userName + " " + file.getName();
        sendClientMessage(message, ClientMessage.MessageType.REQ_FILE);
    }

    /**
     * The writerPrint method user the printWriter to send a message to the server.
     *
     * @param message The message that needs to be send to the server
     */
    private void writerPrint(String message) {
        String encryptedMessage = encyptor.encrypt(message);
        writer.println(encryptedMessage);
        writer.flush();
    }

    PrintWriter getWriter() {
        return writer;
    }
}
