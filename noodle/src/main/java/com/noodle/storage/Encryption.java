package com.noodle.storage;

/**
 * Interface for encrypting and decrypting data.
 */
public interface Encryption {

  /**
   * Cipher the data
   * @param key key of persisted data
   * @param data data to cypher
   * @return cyphered data
   * @throws Exception
   */
  default byte[] encrypt(byte[] key, byte[] data) throws Exception {
    return encrypt(data);
  }

  /**
   * Decrypts the data
   * @param key key of persisted data
   * @param data encrypted data
   * @return decrypted data
   * @throws Exception
   */
  default byte[] decrypt(byte[] key, byte[] data) throws Exception {
    return decrypt(data);
  }

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

  /**
   * Does not encrypt data, just returns the input.
   */
  Encryption NO_ENCRYPTION = new Encryption() {
    @Override
    public byte[] encrypt(byte[] data) throws Exception {
      return data;
    }

    @Override
    public byte[] decrypt(byte[] data) throws Exception {
      return data;
    }
  };
}
