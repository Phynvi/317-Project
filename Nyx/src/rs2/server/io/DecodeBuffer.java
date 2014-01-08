package rs2.server.io;

/**
 * A buffer used to decode incoming data.
 * @author `Discardedx2
 */
public class DecodeBuffer extends Buffer {

	public DecodeBuffer(byte[] data) {
		super(data, true);
	}

	public DecodeBuffer(byte[] data, int position) {
		super(data, position, true);
	}

	public DecodeBuffer(int size) {
		super(size, true);
	}

	public DecodeBuffer(int size, int position) {
		super(size, position, true);
	}

	/**
	 * Reads one byte at the current position.
	 * @return the value of the byte.
	 */
	public int get() {
		return payload[position++];
	}

	/**
	 * Reads one short at the current position.
	 * @return the value of the short.
	 */
	public int getShort() {
		position += 2;
		return ((payload[position - 2] & 0xFF) << 8) | (payload[position - 1] & 0xFF);
	}

}
