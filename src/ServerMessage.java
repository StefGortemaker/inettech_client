public class ServerMessage {

    private String line;

    ServerMessage(String line) {
        this.line = line;
    }

    ServerMessage.MessageType getMessageType() {
        ServerMessage.MessageType result = ServerMessage.MessageType.UNKNOWN;

        try {
            if (line != null && line.length() > 0) {
                String[] splits = line.split("\\s+");
                String lineTypePart = splits[0];
                if (lineTypePart.startsWith("-") || lineTypePart.startsWith("+")) {
                    lineTypePart = lineTypePart.substring(1);
                }

                result = ServerMessage.MessageType.valueOf(lineTypePart);
            }
        } catch (IllegalArgumentException var4) {
            System.out.println("[ERROR] Unknown command");
        }

        return result;
    }

    String getPayload() {
        if (getMessageType().equals(ServerMessage.MessageType.UNKNOWN)) {
            return line;
        } else if (line != null && line.length() >= getMessageType().name().length() + 1) {
            int offset = 0;
            if (getMessageType().equals(ServerMessage.MessageType.OK) ||
                    getMessageType().equals(ServerMessage.MessageType.ERR)) {
                offset = 1;
            }

            return line.substring(getMessageType().name().length() + 1 + offset);
        } else {
            return "";
        }
    }

    public String toString() {
        return line;
    }

    public enum MessageType {
        HELO, BCST, PING, QUIT,
        CLTLIST, PM, GRP_CREATE, GRPLIST, GRP_JOIN, GRP_SEND, GRP_LEAVE, GRP_KICK,
        REQ_FILE, ACCEPT_FILE, DENY_FILE, TRANSFER_FILE,
        OK, ERR, UNKNOWN;

        MessageType(){

        }

    }
}
