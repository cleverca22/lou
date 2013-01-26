package com.angeldsis.louapi.data;

public class BaseLou {
	String data;
	int offset;
	public BaseLou(String x) {
		data = x;
		offset = 0;
	}

	public int readByte() throws Exception {
		int ascii = data.charAt(offset);
		offset++;
		if ((ascii >= 65) && (ascii <= 90)) return ascii - 65;
		if ((ascii >= 97) && (ascii <= 122)) return ascii - 71;
		if ((ascii >= 48) && (ascii <= 57)) return ascii + 4;
		if ((ascii >= 58) && (ascii <= 64)) return ascii + 16;
		else {
			switch (ascii) {
			case 33: return 62;
			case 35: return 63;
			case 36: return 64;
			case 37: return 65;
			case 38: return 66;
			case 40: return 67;
			case 41: return 68;
			case 42: return 69;
			case 43: return 70;
			case 44: return 71;
			case 46: return 72;
			case 32: return 73;
			case 91: return 81;
			case 93: return 82;
			case 94: return 83;
			case 95: return 84;
			case 96: return 85;
			case 123: return 86;
			case 124: return 87;
			case 125: return 88;
			case 126: return 89;
			case 39: return 90;
			case 45: return 91;
			}
			System.out.println("Unkown ascii->byte conversion: "+ascii);
			System.out.println(this.data);
			System.out.println(this.offset);
			throw new Exception("unknown ascii conversion");
		}
	}
	public int read2Bytes() throws Exception {
		int byte1 = readByte();
		int byte2 = readByte();
		return byte1 + (byte2 * 0x5b);
	}
	public int read4Bytes() throws Exception {
		int byte1 = readByte();
		int byte2 = readByte();
		int byte3 = readByte();
		int byte4 = readByte();
		return byte1 + (byte2 * 0x5b) + (byte3 * 0x5b * 0x5b) + (byte4 * 0x5b * 0x5b * 0x5b);
	}

	public int readMultiBytes() throws Exception {
		int bytes = 0;
		int output = 0;
		while (offset < data.length()) {
			if (data.charAt(offset) == '-') {
				offset++;
				return output;
			}
			output += readByte() * Math.pow(0x5b, bytes);
			bytes++;
		}
		throw new Exception("- not found");
	}

	public String readRest() {
		return data.substring(offset);
	}

	public int read3Bytes() throws Exception {
		int byte1 = readByte();
		int byte2 = readByte();
		int byte3 = readByte();
		return byte1 + (byte2 * 0x5b) + (byte3 * 0x5b * 0x5b);
	}
}
