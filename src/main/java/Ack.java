final class Ack extends Packet {

    protected Ack() {
    }

    public Ack(int blockNumber) {
        length = 4;
        this.message = new byte[length];
        put(opOffset, ACK);
        put(blkOffset, (short) blockNumber);
    }

    public int blockNumber() {
        return this.get(blkOffset);
    }
}