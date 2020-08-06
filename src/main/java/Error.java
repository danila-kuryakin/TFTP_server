class Error extends Packet {

    protected Error() {
    }

    public Error(int number, String message) {

        length = 4 + message.length() + 1;
        this.message = new byte[length];
        put(opOffset, ERROR);
        put(numOffset, (short) number);
        put(msgOffset, message, (byte) 0);
    }

    public int number() {
        return this.get(numOffset);
    }
    public String message() {
        return this.get(msgOffset, (byte) 0);
    }
}