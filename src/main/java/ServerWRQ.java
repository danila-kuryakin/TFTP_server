import java.net.*;
import java.io.*;
import java.nio.charset.Charset;


class ServerWRQ extends Thread {

    protected DatagramSocket sock;
    protected InetAddress host;
    protected int port;
    protected FileOutputStream outFile;
    protected Packet req;
    protected int timeoutLimit = 5;

    protected File saveFile;
    protected String fileName;
    protected int blockSize;
    protected String mode;
    protected String option;



    public ServerWRQ(Write request) throws Exception {
        try {
            req = request;
            sock = new DatagramSocket();
            sock.setSoTimeout(1000);

            host = request.getAddress();
            port = request.getPort();
            fileName = request.fileName();

            saveFile = new File(fileName);
            outFile = new FileOutputStream(saveFile, false);


            String fName = request.get(2, (byte) 0);
            mode = request.get(2 + fName.length() + 1, (byte) 0);
            if (mode.equals("octet")){
                option = request.get(2 + fName.length() + 1 + mode.length() + 1, (byte) 0);
                blockSize = Integer.valueOf(request.get(2 + fName.length() + 1
                        + mode.length() + 1 + option.length() + 1, (byte) 0));
                Oack Oack = new Oack(option, blockSize);
                Oack.send(host, port, sock);
            } else {
                blockSize = 512;
                Ack ack = new Ack(0);
                ack.send(host, port, sock);
            }
            System.out.println("Opcode " + request.get(request.opOffset) + ", File name " + fName
                    + ", Mode " + mode + ", Block size " + blockSize);

                this.start();

        } catch (Exception e) {
            Error ePak = new Error(1, e.getMessage()); // error code 1
            try {
                ePak.send(host, port, sock);
            } catch (Exception f) {
            }

            System.out.println("Client start failed:" + e.getMessage());
        }
    }

    public void run() {

        int blkNum, bytesOut;
        if (req instanceof Write) {
            try {
                for (blkNum = 1, bytesOut = blockSize; bytesOut == blockSize /*|| bytesOut == 512*/ ; blkNum++) {
                    while (timeoutLimit != 0) {
                        try {
                            Packet inPak = Packet.receive(sock, blockSize);

                            System.out.println();

                            if (inPak instanceof Error) {
                                Error p = (Error) inPak;
                                throw new Exception(p.message());
                            } else if (inPak instanceof Data) {
                                Data p = (Data) inPak;

                                if (p.blockNumber() != blkNum) {
                                    throw new SocketTimeoutException();
                                }

                                bytesOut = p.write(outFile);

                                System.out.println("bytesOut " + bytesOut);
                                Ack a = new Ack(blkNum);
                                a.send(host, port, sock);
                                break;
                            }
                        } catch (SocketTimeoutException t2) {
                            System.out.println("Time out, resend ack");
                            Ack a = new Ack(blkNum - 1);
                            a.send(host, port, sock);
                            timeoutLimit--;
                        }
                    }
                    if(timeoutLimit==0){throw new Exception("Connection failed");}
                }
                System.out.println("Transfer completed.(Client " +host +")" );

//                System.out.println("SHA1 checksum: "+CheckSum.getChecksum(fileName)+"\n");

                System.out.println("Blocks transfer: " + (blkNum-1));

                outFile.close();

            } catch (Exception e) {
                Error ePak = new Error(1, e.getMessage());
                try {
                    ePak.send(host, port, sock);
                } catch (Exception f) {
                }

                System.out.println("Client failed:  " + e.getMessage());
                saveFile.delete();
            }
        }
    }
}

