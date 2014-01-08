package rs2.server.io;

/**
 * A representation of a memory buffer.
 * @author `Discardedx2
 */
public abstract class Buffer {

	/**
	 * This buffer's payload.
	 */
	protected byte[] payload;
	/**
	 * This buffer's position.
	 */
	protected int position;
	/**
	 * The read only value.
	 */
	private boolean readOnly;
	
	/**
	 * Allocates a new buffer with a specified.
	 * @param data The data to copy into the buffer.
	 */
	public Buffer(byte[] data, int position, boolean readOnly) {
		this.payload = data;
		this.position = position;
		this.readOnly = readOnly;
	}
	
	public Buffer(byte[] data, boolean readOnly) {
		this(data, 0, readOnly);
	}
	
	public Buffer(int size, boolean readOnly) {
		this(new byte[size], 0, readOnly);
	}
	
	public Buffer(int size, int position, boolean readOnly) {
		this(new byte[size], position, readOnly);
	}

	/**
	 * Gets the payload of this buffer.
	 * @return the payload.
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * Gets the position of the buffer.
	 * @return the position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Gets the readOnly value.
	 * @return the readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	
}
