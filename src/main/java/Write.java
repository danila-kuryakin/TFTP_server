final class Write extends Packet {

    protected Write() {}

    public String fileName() {
        return this.get(fileOffset,(byte)0);
    }

    public String requestType() {
        String fname = fileName();
        return this.get(fileOffset+fname.length()+1,(byte)0);
    }
}