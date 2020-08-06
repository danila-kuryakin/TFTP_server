final class Read extends Packet {

    protected Read() {}

    public String fileName() {
        return this.get(fileOffset,(byte)0);
    }

    public String requestType() {
        String fname = fileName();
        return this.get(fileOffset+fname.length()+1,(byte)0);
    }
}
