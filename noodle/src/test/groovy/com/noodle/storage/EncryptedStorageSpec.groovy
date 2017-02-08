package com.noodle.storage

import com.noodle.encryption.Encryption
import org.robospock.RoboSpecification

/**
 *
 */
class EncryptedStorageSpec extends RoboSpecification {

  private final Encryption xorEncryption = new Encryption() {

    @Override
    byte[] encrypt(final byte[] src) throws Exception {
      def bytes = new byte[src.length]
      for (int i = 0; i < src.length; i++) {
        bytes[i] = 0x8A ^ src[i]
      }
      return bytes
    }

    @Override
    byte[] decrypt(final byte[] data) throws Exception {
      return encrypt(data)
    }
  }
  private ByteBufferStorage storage

  void setup() {
    storage = new ByteBufferStorage(xorEncryption)
  }

  def "should use encryption when storing data"() {
    given:
    def mockEncryption = Mock(Encryption)
    storage = new ByteBufferStorage(mockEncryption)

    def keyBytes = "1".bytes
    def dataBytes = "a".bytes

    when:
    storage.put(new Record(keyBytes, dataBytes))

    then:
    1 * mockEncryption.encrypt(keyBytes) >> keyBytes
    1 * mockEncryption.encrypt(dataBytes) >> dataBytes

  }

  def "should use decryption when getting data"() {
    given:
    def mockEncryption = Mock(Encryption)
    storage = new ByteBufferStorage(mockEncryption)

    def keyBytes = "1".bytes
    def dataBytes = "a".bytes

    mockEncryption.encrypt(keyBytes) >> keyBytes
    mockEncryption.encrypt(dataBytes) >> dataBytes

    storage.put(new Record(keyBytes, dataBytes))

    when:
    storage.get(keyBytes)

    then:
    1 * mockEncryption.decrypt(keyBytes) >> keyBytes
    1 * mockEncryption.decrypt(dataBytes) >> dataBytes

  }

  def "should put and get same item"() {
    given:
    def r = new Record("123".bytes, "abcdefghjklmnopqrstuvwxyz".bytes)

    when:
    storage.put(r)

    then:
    storage.get(r.key) == r
  }
}
