public class Oack extends Packet {

    public Oack(){}

    public Oack(String option, int sizeBlock) {
        String block =  String.valueOf(sizeBlock);
        length = 2 + option.length() + 1 + block.length() + 1;
        this.message = new byte[length];
        put(opOffset, (short) 6);
        put(2, option, (byte) 0);
        put(2 + option.length() + 1, String.valueOf(sizeBlock), (byte) 0);
    }


}
