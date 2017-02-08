package com.noodle.encryption;

/**
 * Interface for encrypting and decrypting data.
 */
public interface Encryption {

  /**
   * Cipher the data
   * @param data data to cypher
   * @return cyphered data
   * @throws Exception
   */
  byte[] encrypt(byte[] data) throws Exception;

  /**
   * Decrypts the data
   * @param data encrypted data
   * @return decrypted data
   * @throws Exception
   */
  byte[] decrypt(byte[] data) throws Exception;
}
