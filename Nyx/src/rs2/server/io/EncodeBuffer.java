package rs2.server.io;

public class EncodeBuffer extends Buffer {
	
	public EncodeBuffer(byte[] data) {
		super(data, false);
	}

	public EncodeBuffer(byte[] data, int position) {
		super(data, position, false);
	}

	public EncodeBuffer(int size) {
		super(size, false);
	}

	public EncodeBuffer(int size, int position) {
		super(size, position, false);
	}
	
}
