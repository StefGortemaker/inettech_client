import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerReader implements Runnable{

  private Socket socket;

  ServerReader(Socket socket) {
    this.socket = socket;
  }

  private enum Message {
    HELO, BCST, PONG, QUIT,
    CLTLIST, PM, GRP_CREATE, GRPLIST, GRP_JOIN, GRP_SEND, GRP_LEAVE, GRP_KICK
  }

  @Override
  public void run() {

    while (true){
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(reader.readLine());
      } catch (IOException e) {
        e.printStackTrace();
      }

      //      switch (input) {
//        case HELO:
//          break;
//        case BCST:
//          break;
//        case PONG:
//          break;
//        case QUIT:
//          break;
//        case CLTLIST:
//          break;
//        case PM:
//          break;
//        case GRP_CREATE:
//          break;
//        case GRPLIST:
//          break;
//        case GRP_JOIN:
//          break;
//        case GRP_SEND:
//          break;
//        case GRP_LEAVE:
//          break;
//        case GRP_KICK:
//          break;
//        default:
//          break;
//      }
    }
  }
}
