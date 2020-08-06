import java.net.*;

public class StartServer {

    public static void main(String argv[]) {
        try {

            DatagramSocket sock = new DatagramSocket(69);
            System.out.println("Server Ready.  Port:  " + sock.getLocalPort());

            while (true) {
                Packet in = Packet.receive(sock, 512);
                if (in instanceof Read) {
                    System.out.println("Read Request from " + in.getAddress());
                    ServerRRQ r = new ServerRRQ((Read) in);
                }

                else if (in instanceof Write) {
                    System.out.println("Write Request from " + in.getAddress());
                    ServerWRQ w = new ServerWRQ((Write) in);
                }
            }
        } catch (SocketException e) {
            System.out.println("Server terminated(SocketException) " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Server terminated(Exception)" + e.getMessage());
        }
    }
}