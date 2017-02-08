package com.noodle.encryption;

/**
 * Does not encrypt data, just returns the input.
 */
public class NoEncryption implements Encryption {

  @Override
  public byte[] encrypt(final byte[] data) throws Exception {
    return data;
  }

  @Override
  public byte[] decrypt(final byte[] data) throws Exception {
    return data;
  }
}
