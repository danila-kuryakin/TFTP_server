import java.net.*;
import java.io.*;


class ServerRRQ extends Thread {

    protected DatagramSocket sock;
    protected InetAddress host;
    protected int port;
    protected FileInputStream source;
    protected Packet req;
    protected String fileName;
    protected int blockSize;
    protected String mode;
    protected String option;
    protected long lenFile;

    public ServerRRQ(Read request) throws Exception {
        try {
            req = request;

            sock = new DatagramSocket();
            sock.setSoTimeout(1000);
            fileName = request.fileName();

            host = request.getAddress();
            port = request.getPort();

            String fName = request.get(2, (byte) 0);
            mode = request.get(2 + fName.length()+1, (byte) 0);
        if (mode.equals("octet")){
            option = request.get(2 + fName.length() + 1 + mode.length() + 1, (byte) 0);
            blockSize = Integer.valueOf(request.get(2 + fName.length() + 1
                    + mode.length() + 1 + option.length() + 1, (byte) 0));
        } else {
            blockSize = 512;
        }
        System.out.println("Opcode " + request.get(request.opOffset) + ", File name " + fName
                + ", Mode " + mode + ", Block size " + blockSize);


            File srcFile = new File(fileName);
            lenFile = srcFile.length();

            if (srcFile.exists() && srcFile.isFile() && srcFile.canRead()) {
                source = new FileInputStream(srcFile);
                this.start();

            } else
                throw new Exception("access violation");

        } catch (Exception e) {
            Error ePak = new Error(1, e.getMessage());
            try {
                ePak.send(host, port, sock);
            } catch (Exception f) {
            }

            System.out.println("Client start failed:  " + e.getMessage());
        }
    }

    public void run() {
        int timeoutLimit=5;

        int bytesRead = blockSize;

        int blkNum;

        if (req instanceof Read) {
            try {
                for (blkNum = 1; bytesRead == blockSize; blkNum++) {

                    Data outPak = new Data(blkNum, source, blockSize);
                    bytesRead = outPak.getLength()-4;

                    outPak.send(host, port, sock);

                    while (timeoutLimit!=0) {
                        try {

                            Packet ack = Packet.receive(sock, blockSize);
                            if (!(ack instanceof Ack)){throw new Exception("Client failed");}
                            Ack a = (Ack) ack;
                            System.out.println("bytesOut " + bytesRead);

                            if(a.blockNumber()!=blkNum){
                                throw new SocketTimeoutException("Last packet lost, resend packet");}

                            timeoutLimit = 5;
                            break;
                        }
                        catch (SocketTimeoutException t) {
                            System.out.println("Send the block again " + blkNum);
                            timeoutLimit--;
                            outPak.send(host, port, sock);
                        }
                    }
                    if(timeoutLimit==0){throw new Exception("connection failed");}
                }

                System.out.println("Transfer completed.(Client " +host +")" );
                System.out.println("Blocks transfer: " + (blkNum-1));
//                System.out.println("SHA1 checksum: " + CheckSum.getChecksum(fileName)+"\n");
                source.close();
            } catch (Exception e) {
                Error ePak = new Error(1, e.getMessage());

                try {
                    ePak.send(host, port, sock);
                } catch (Exception f) {
                }

                System.out.println("Client failed:  " + e.getMessage());
            }
        }
    }
}