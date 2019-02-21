public class ClientMessage {
        private ClientMessage.MessageType type;
        private String line;

        ClientMessage(ClientMessage.MessageType type, String line) {
            this.type = type;
            this.line = line;
        }

        public String toString() {
            return this.type + " " + this.line;
        }

        public static enum MessageType {
            BSCT, CLTLIST, GRP_CREATE, GRP_JOIN, GRP_KICK, GRP_LEAVE, GRP_LIST, GRP_SEND, HELO, PM, PONG, QUIT;

            private MessageType() {
            }
        }
}
