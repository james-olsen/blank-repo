package platformIndependentCore.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import platformIndependentCore.exceptions.WrappedException;

/**
 * <b>Name :</b> CryptUtils.java
 * <p>
 * <b>Generated :</b> Oct 20, 2020
 * <p>
 * <b>Description :</b>Will handle encrypting and decrypting values given the
 * key file
 * <p>
 *
 * @since Oct 20, 2020
 * @author vbaaustaylol
 */
public class CryptoUtils {
	/** Algorithm for Key */
	public static final String AES = "AES";

	/**
	 * encrypt a value and generate a keyfile if the keyfile is not found then a new
	 * one is created
	 *
	 * @param value   to encrypt
	 * @param keyFile for encryption
	 * @return String the encrypted value
	 */
	public static String encrypt(String value, File keyFile) {
		byte[] encrypted = {};

		try {
			if (!keyFile.exists()) {
				FileWriter fw = null;
				try {
					KeyGenerator keyGen = KeyGenerator.getInstance(CryptoUtils.AES);
					keyGen.init(128);
					SecretKey sk = keyGen.generateKey();
					fw = new FileWriter(keyFile);
					fw.write(byteArrayToHexString(sk.getEncoded()));
				} catch (GeneralSecurityException | IOException e) {
					throw new WrappedException(e);
				} finally {
					// make sure that the file writer gets closed
					if (fw != null) {
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			SecretKeySpec sks = getSecretKeySpec(keyFile);
			Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
			cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
			encrypted = cipher.doFinal(value.getBytes());
		} catch (GeneralSecurityException e) {
			throw new WrappedException(e);
		}
		return byteArrayToHexString(encrypted);
	}

	/**
	 * Will return the decrypted value
	 *
	 * @param message to decrypt
	 * @param keyFile for encryption
	 * @return the decrypted value
	 */
	public static String decrypt(String message, File keyFile) {
		byte[] decrypted = {};
		try {
			SecretKeySpec sks = getSecretKeySpec(keyFile);
			Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
			cipher.init(Cipher.DECRYPT_MODE, sks);
			decrypted = cipher.doFinal(hexStringToByteArray(message));
		} catch (GeneralSecurityException e) {
			throw new WrappedException(e);
		}
		return new String(decrypted);
	}

	/**
	 * Returns a SecretKeySpec from the provided key file
	 *
	 * @param keyFile for encryption
	 * @return SecretKeySpec key
	 */
	private static SecretKeySpec getSecretKeySpec(File keyFile) {
		byte[] key = readKeyFile(keyFile);
		SecretKeySpec sks = new SecretKeySpec(key, CryptoUtils.AES);
		return sks;
	}

	/**
	 * Will read the key file as a byte array
	 *
	 * @param keyFile for encryption
	 * @return byte[] key
	 */
	private static byte[] readKeyFile(File keyFile) {
		String keyValue = "";
		Scanner scanner = null;
		try {
			scanner = new Scanner(keyFile);
			scanner.useDelimiter("\\Z");
			keyValue = scanner.next();
		} catch (FileNotFoundException e) {
			throw new WrappedException(e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return hexStringToByteArray(keyValue);
	}

	/**
	 * Will convert the byte array to a Hex String
	 *
	 * @param bytes byte array
	 * @return String hex value
	 */
	private static String byteArrayToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Will convert a Hex String to a byte array
	 *
	 * @param hexString String value
	 * @return byte[] bytes
	 */
	private static byte[] hexStringToByteArray(String hexString) {
		byte[] b = new byte[hexString.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(hexString.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

}
