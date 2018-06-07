
public class TirunagaruP3Tea  {
	private final static int DELTA = 0x9E3779B9;
	private final static int KEYLENGTH  = 32;
	private final static int REVERSEDELTA = 0xC6EF3720;

	private int[] SecretKey = new int[4];

	// Initialize the cipher for encryption or decryption.
	public TirunagaruP3Tea(byte[] key) {
		if (key == null)
			throw new RuntimeException("Invalid key: Key was null");
		//if (key.length < 16) throw new RuntimeException("Invalid key: Length was less than 16 bytes");
		for (int off=0, i=0; i<4; i++) {
			SecretKey[i] = ((key[off++] & 0xff)) |
			((key[off++] & 0xff) <<  8) |
			((key[off++] & 0xff) << 16) |
			((key[off++] & 0xff) << 24);
		}
	}

	//Encrypt an array of bytes
	public byte[] encrypt(byte[] clear) 
	{
		System.out.println("---- Encrypting Data: "+ new String(clear).substring(2).trim()+" ----");
		
		int paddedSize = ((clear.length/8) + (((clear.length%8)==0)?0:1)) * 2;
		int[] buffer = new int[paddedSize + 1];
		buffer[0] = clear.length;
		int destOffset = 1,shift = 24;
		int i = 0;
		//normal,padded, 1		
			assert destOffset + (clear.length / 4) <= buffer.length;
			int j = destOffset;
			buffer[j] = 0;
			while (i<clear.length) 
			{
				buffer[j] |= ((clear[i] & 0xff) << shift);
				if (shift==0)
				{
					shift = 24;
					j++;
					if (j<buffer.length) buffer[j] = 0;
				}
				else { shift -= 8; }
				i++;
			}	
			assert buffer.length % 2 == 1;
			int s, v0, v1, sum, n;
			s = 1;
			while (s<buffer.length) {
				n = KEYLENGTH;
				v0 = buffer[s];
				v1 = buffer[s+1];
				sum = 0;
				while (n-->0) {
					sum += DELTA;
					v0  +=  (sum ^ (v1 >>> 3)) + SecretKey[1] +((v1 << 4 ) + SecretKey[0] ^ v1);
					v1  +=  (sum ^ (v0 >>> 2)) + SecretKey[3] +((v0 << 6 ) + SecretKey[2] ^ v0) ;
				}
				buffer[s] = v0;
				buffer[s+1] = v1;
				s+=2;
			}	
		//en, 0, en.length*4
		int srcOffset =0;
		int destLength =buffer.length * 4;
		assert destLength <= (buffer.length - srcOffset) * 4;
		byte[] dest = new byte[destLength];
		int k = srcOffset;
		int count = 0;
		for (int l = 0; l < destLength; l++) {
			dest[l] = (byte) ((buffer[k] >> (24 - (8*count))) & 0xff);
			count++;
			if (count == 4) { count = 0; k++; }
		}
		return dest;	
	}

	
	 // Decrypt an array of bytes
	public byte[] decrypt(byte[] crypt) {
		
		assert crypt.length % 4 == 0;
		assert (crypt.length / 4) % 2 == 1;
		int[] buffer = new int[crypt.length / 4];
		int destOffset =0;
		//normal,padded, 1
			assert destOffset + (crypt.length / 4) <= buffer.length;
			int k = 0, shift = 24;
			int l = destOffset;
			buffer[l] = 0;
			while (k<crypt.length) {
				buffer[l] |= ((crypt[k] & 0xff) << shift);
				if (shift==0) {
					shift = 24;
					l++;
					if (l<buffer.length) buffer[l] = 0;
				}
				else { shift -= 8; }
				k++;
			}
			assert buffer.length % 2 == 1;
			int s, v0, v1, sum, n;
			s = 1;
			while (s<buffer.length) {
				n = KEYLENGTH;
				v0 = buffer[s]; 
				v1 = buffer[s+1];
				sum = REVERSEDELTA;
				while (n--> 0) {
					v1  -= (sum ^ (v0 >>> 2)) + SecretKey[3] +((v0 << 6 ) + SecretKey[2] ^ v0) ;
					v0  -=  (sum ^ (v1 >>> 3)) + SecretKey[1]+((v1 << 4 ) + SecretKey[0] ^ v1) ;
					sum -= DELTA;
				}
				buffer[s] = v0;
				buffer[s+1] = v1;
				s+=2;
			}
			int srcOffset =1;
			int destLength =buffer[0];
			assert destLength <= (buffer.length - srcOffset) * 4;
			byte[] dest = new byte[destLength];
			int i = srcOffset;
			int count = 0;
			for (int j = 0; j < destLength; j++) {
				dest[j] = (byte) ((buffer[i] >> (24 - (8*count))) & 0xff);
				count++;
				if (count == 4) { count = 0; i++; }
			}
			System.out.println("---- Decrypted Data: "+ new String(dest).substring(2).trim()+" ----");
			return dest;	
	}
	}
