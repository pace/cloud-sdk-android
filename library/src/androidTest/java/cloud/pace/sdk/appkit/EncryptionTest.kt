package cloud.pace.sdk.appkit

import cloud.pace.sdk.appkit.utils.EncryptionUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class EncryptionTest {

    @Test
    fun testEncryption() {
        val test = "testtest"
        val encrypted = EncryptionUtils.encrypt(test)
        val decrypted = EncryptionUtils.decrypt(encrypted)
        assertEquals(test, decrypted)

        // check if it also works two times in a row
        val test2 = "another test"
        val encrypted2 = EncryptionUtils.encrypt(test2)
        val decrypted2 = EncryptionUtils.decrypt(encrypted2)
        assertEquals(test2, decrypted2)
    }
}
