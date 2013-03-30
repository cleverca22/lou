package com.angeldsis.louapi.data;

import com.angeldsis.louapi.Log;

public class BaseLou {
	public String data;
	public int offset;
	StringBuilder output;
	public BaseLou(String x) {
		data = x;
		offset = 0;
	}

	public BaseLou() {
		output = new StringBuilder();
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
			if (bytes == 5) return output;
		}
		throw new Exception("- not found: "+data.substring(offset));
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
	public void write2Bytes(int in) throws Exception {
		int byte1 = in % 0x5b;
		int byte2 = in / 0x5b;
		writeByte(byte1);
		writeByte(byte2);
	}
	private void writeByte(int in) throws Exception {
		//Log.v("BaseLou","writeByte("+in+")");
		char out;
		if ((in >= 0) && (in <=25)) out = (char) (in + 65);
		else if ((in >= 26) && (in <= 51)) out = (char) (in + 71);
		else if ((in >= 52) && (in <= 61)) out = (char) (in -4);
		else if ((in >= 63) && (in <= 66)) out = (char) (in - 28);
		else if ((in >= 67) && (in <= 71)) out = (char) (in - 27);
		else if ((in >= 74) && (in <= 80)) out = (char) (in - 16);
		else if ((in >= 82) && (in <= 85)) out = (char) (in + 11);
		else if ((in >= 86) && (in <= 89)) out = (char) (in + 37);
		else {
			switch (in) {
			case 62: out = 33;break;
			case 72:
				out = 46;
				break;
			case 73: out = 32;break;
			case 81: out = 91;break;
			case 90: out = 39;break;
			default:
				throw new Exception("unknown ascii conversion "+in);
			}
		}
		output.append(out);
	}
	public void writeManyBytes(int i) throws Exception {
		while (true) {
			writeByte(i % 0x5b);
			i = i / 0x5b;
			if (i == 0) {
				output.append('-');
				return;
			}
		}
	}
	public String getOutput() {
		return output.toString();
	}
}
