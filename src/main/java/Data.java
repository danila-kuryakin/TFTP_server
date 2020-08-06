import java.io.*;

final class Data extends Packet {

    protected Data() {}
    public Data(int blockNumber, FileInputStream in, int blockSize) throws IOException {
        this.maxData = blockSize;
        this.maxPacketLen = this.maxData + 4;
        this.length = this.maxPacketLen;
        this.message = new byte[this.maxPacketLen];

        this.put(opOffset, DATA);
        this.put(blkOffset, (short) blockNumber);

        this.length = in.read(this.message, this.dataOffset, this.maxData) + 4;
    }

    public int blockNumber() {
        return this.get(blkOffset);
    }

    public int write(FileOutputStream out) throws IOException {
        out.write(message, dataOffset, length - 4);

        return (length - 4);
    }

    public int write(OutputStreamWriter writer) throws IOException {

        writer.write(String.valueOf(message), dataOffset, length - 4);

        return (length - 4);
    }
}