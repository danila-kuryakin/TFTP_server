import java.io.*;
import java.net.*;

public class Packet {

    protected static final short RRQ = 1;
    protected static final short WRQ = 2;
    protected static final short DATA = 3;
    protected static final short ACK = 4;
    protected static final short ERROR = 5;
    protected static final short OACK = 6;

    protected static final int opOffset=0;

    protected static final int fileOffset=2;

    protected static final int blkOffset=2;
    protected static final int dataOffset=4;

    protected static final int numOffset=2;
    protected static final int msgOffset=4;

    public int maxData;
    public int maxPacketLen;

    protected byte [] message;
    protected int length;

    protected InetAddress host;
    protected int port;

    public Packet() {
        this.maxData = 512;
        this.maxPacketLen = this.maxData + 4;
        message=new byte[maxPacketLen];
        length= maxPacketLen;
    }

    public Packet(int maxData) {
        this.maxData = maxData;
        this.maxPacketLen = this.maxData + 4;
        message=new byte[maxPacketLen];
        length= maxPacketLen;
    }

    public static Packet receive(DatagramSocket sock, int blockSize) throws IOException {
        Packet in=new Packet(blockSize), retPak=new Packet(blockSize);
        DatagramPacket inPak = new DatagramPacket(in.message,in.length);
        sock.receive(inPak);

        switch (in.get(0)) {
            case RRQ:
                retPak=new Read();
                break;
            case WRQ:
                retPak=new Write();
                break;
            case DATA:
                retPak=new Data();
                break;
            case ACK:
                retPak=new Ack();
                break;
            case ERROR:
                retPak=new Error();
                break;
            case OACK:
                retPak=new Oack();
                break;
        }
        retPak.message=in.message;
        retPak.length=inPak.getLength();
        retPak.host=inPak.getAddress();
        retPak.port=inPak.getPort();
        return retPak;
    }

    public void send(InetAddress ip, int port, DatagramSocket s) throws IOException {
        s.send(new DatagramPacket(message,length,ip,port));
    }

    public InetAddress getAddress() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getLength() {
        return length;
    }

    // Methods to put opcode, blkNum, error code into the byte array 'message'.
    protected void put(int startByte, short value) {
        message[startByte++] = (byte)(value >>> 8);  // first byte
        message[startByte] = (byte)(value % 256);    // last byte
    }

    protected void put(int startByte, String value, byte del) {
        value.getBytes(0, value.length(), message, startByte);
        message[startByte + value.length()] = del;
    }

    protected int get(int startByte) {
        return (message[startByte] & 0xff) << 8 | message[startByte+1] & 0xff;
    }

    protected String get (int startByte, byte del) {
        StringBuffer result = new StringBuffer();
        while (message[startByte] != del) result.append((char)message[startByte++]);
        return result.toString();
    }
}