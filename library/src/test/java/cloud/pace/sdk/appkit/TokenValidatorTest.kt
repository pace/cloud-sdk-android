package cloud.pace.sdk.appkit

import android.os.Build
import cloud.pace.sdk.appkit.utils.TokenValidator
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class TokenValidatorTest {

    @Test
    fun `token is not expired`() {
        val isValid = TokenValidator.isTokenValid(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjQwNzA5MDg4MDAsImlhdCI6MTY0MzIxNTcwNCwiYXV0aF90aW1lIjoxNjQzMjA2MzgyLCJqdGkiOiIwMTg0YTM4Yy02YzdkLTRiMDEtYjhiYi1mY2ZlYjAyM2M5NjY" +
                "iLCJpc3MiOiJodHRwczovL2lkLmRldi5wYWNlLmNsb3VkL2F1dGgvcmVhbG1zL3BhY2UiLCJzdWIiOiI3M2VlYWUyZC1mMTE4LTRkZjMtOTFkZS1jZDAwZDM4OGQwNDkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJjbG91ZC1zZGstZXh" +
                "hbXBsZS1hcHAiLCJub25jZSI6Ii1Rc3g4UHZxUHV6eFpTeDh3bkJKNHciLCJzZXNzaW9uX3N0YXRlIjoiY2IyMDM1ZjQtMGM3Ni00OTVjLTljYjAtYmE2MmExNTU1ZWMxIiwiYWNyIjoiMCIsInNjb3BlIjoib3BlbmlkIHBvaTphcHB" +
                "zOnJlYWQgZnVlbGluZzp0cmFuc2FjdGlvbnM6Y3JlYXRlIHBheTphcHBsZXBheS1zZXNzaW9uczpjcmVhdGUgcGF5OnBheW1lbnQtbWV0aG9kczpjcmVhdGUgdXNlcjpwcmVmZXJlbmNlczpyZWFkOnBheW1lbnQtYXBwIGZ1ZWxpbmc" +
                "6ZGlzY291bnRzOmlucXVpcnkgcGF5OnBheW1lbnQtbWV0aG9kczpwYXRjaCB1c2VyOmRldmljZS10b3RwczpjcmVhdGUgdXNlcjp1c2Vycy5waW46Y2hlY2sgcGF5OnRyYW5zYWN0aW9uczpyZWFkIGZ1ZWxpbmc6Z2FzLXN0YXRpb25" +
                "zOmFwcHJvYWNoaW5nIHBheTpwYXltZW50LXRva2VuczpkZWxldGUgcG9pOmdhcy1zdGF0aW9uczpyZWFkIHVzZXI6cHJlZmVyZW5jZXM6cmVhZCBmdWVsaW5nOnB1bXBzOnJlYWQgdXNlcjpvdHA6dmVyaWZ5IHVzZXI6b3RwOmNyZWF" +
                "0ZSB1c2VyOnByZWZlcmVuY2VzOndyaXRlIHBheTpwYXltZW50LXRva2VuczpjcmVhdGUgcGF5OnRyYW5zYWN0aW9uczpyZWNlaXB0IHVzZXI6dXNlcnMucGluOnVwZGF0ZSB1c2VyY3JlZGl0OnVzZXIuY3JlZGl0OnJlYWQgdXNlcjp" +
                "kZXZpY2UtdG90cHM6Y3JlYXRlLWFmdGVyLWxvZ2luIHVzZXI6dXNlcnMucGFzc3dvcmQ6Y2hlY2sgdXNlcjp1c2VyLmVtYWlsOnJlYWQgcGF5OnBheW1lbnQtbWV0aG9kczpkZWxldGUgcGF5OnBheW1lbnQtbWV0aG9kczpyZWFkIGZ" +
                "1ZWxpbmc6dHJhbnNhY3Rpb25zOmRlbGV0ZSB1c2VyOnRlcm1zOmFjY2VwdCB1c2VyOnVzZXIubG9jYWxlOnJlYWQgcGF5OnBheW1lbnQtbWV0aG9kczpjcmVhdGU6ZGt2Iiwic2lkIjoiY2IyMDM1ZjQtMGM3Ni00OTVjLTljYjAtYmE" +
                "2MmExNTU1ZWMxIiwiem9uZWluZm8iOiJFdXJvcGUvQmVybGluIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImxvY2FsZSI6ImVuIiwiZW1haWwiOiJob3JzdEBwYWNlLmNhciJ9.cF9ikMNV5ADxqSe_Lk5UlM_fGtIGoTp1N7UXrS5ebmM"
        )
        assertTrue(isValid)
    }

    @Test
    fun `token is expired`() {
        val isValid = TokenValidator.isTokenValid(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NDMyMTU3NjQsImlhdCI6MTY0MzIxNTcwNCwiYXV0aF90aW1lIjoxNjQzMjA2MzgyLCJqdGkiOiIwMTg0YTM4Yy02YzdkLTRiMDEtYjhiYi1mY2ZlYjAyM2M5NjY" +
                "iLCJpc3MiOiJodHRwczovL2lkLmRldi5wYWNlLmNsb3VkL2F1dGgvcmVhbG1zL3BhY2UiLCJzdWIiOiI3M2VlYWUyZC1mMTE4LTRkZjMtOTFkZS1jZDAwZDM4OGQwNDkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJjbG91ZC1zZGstZXh" +
                "hbXBsZS1hcHAiLCJub25jZSI6Ii1Rc3g4UHZxUHV6eFpTeDh3bkJKNHciLCJzZXNzaW9uX3N0YXRlIjoiY2IyMDM1ZjQtMGM3Ni00OTVjLTljYjAtYmE2MmExNTU1ZWMxIiwiYWNyIjoiMCIsInNjb3BlIjoib3BlbmlkIHBvaTphcHB" +
                "zOnJlYWQgZnVlbGluZzp0cmFuc2FjdGlvbnM6Y3JlYXRlIHBheTphcHBsZXBheS1zZXNzaW9uczpjcmVhdGUgcGF5OnBheW1lbnQtbWV0aG9kczpjcmVhdGUgdXNlcjpwcmVmZXJlbmNlczpyZWFkOnBheW1lbnQtYXBwIGZ1ZWxpbmc" +
                "6ZGlzY291bnRzOmlucXVpcnkgcGF5OnBheW1lbnQtbWV0aG9kczpwYXRjaCB1c2VyOmRldmljZS10b3RwczpjcmVhdGUgdXNlcjp1c2Vycy5waW46Y2hlY2sgcGF5OnRyYW5zYWN0aW9uczpyZWFkIGZ1ZWxpbmc6Z2FzLXN0YXRpb25" +
                "zOmFwcHJvYWNoaW5nIHBheTpwYXltZW50LXRva2VuczpkZWxldGUgcG9pOmdhcy1zdGF0aW9uczpyZWFkIHVzZXI6cHJlZmVyZW5jZXM6cmVhZCBmdWVsaW5nOnB1bXBzOnJlYWQgdXNlcjpvdHA6dmVyaWZ5IHVzZXI6b3RwOmNyZWF" +
                "0ZSB1c2VyOnByZWZlcmVuY2VzOndyaXRlIHBheTpwYXltZW50LXRva2VuczpjcmVhdGUgcGF5OnRyYW5zYWN0aW9uczpyZWNlaXB0IHVzZXI6dXNlcnMucGluOnVwZGF0ZSB1c2VyY3JlZGl0OnVzZXIuY3JlZGl0OnJlYWQgdXNlcjp" +
                "kZXZpY2UtdG90cHM6Y3JlYXRlLWFmdGVyLWxvZ2luIHVzZXI6dXNlcnMucGFzc3dvcmQ6Y2hlY2sgdXNlcjp1c2VyLmVtYWlsOnJlYWQgcGF5OnBheW1lbnQtbWV0aG9kczpkZWxldGUgcGF5OnBheW1lbnQtbWV0aG9kczpyZWFkIGZ" +
                "1ZWxpbmc6dHJhbnNhY3Rpb25zOmRlbGV0ZSB1c2VyOnRlcm1zOmFjY2VwdCB1c2VyOnVzZXIubG9jYWxlOnJlYWQgcGF5OnBheW1lbnQtbWV0aG9kczpjcmVhdGU6ZGt2Iiwic2lkIjoiY2IyMDM1ZjQtMGM3Ni00OTVjLTljYjAtYmE" +
                "2MmExNTU1ZWMxIiwiem9uZWluZm8iOiJFdXJvcGUvQmVybGluIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImxvY2FsZSI6ImVuIiwiZW1haWwiOiJob3JzdEBwYWNlLmNhciJ9.TjF76khCQ5C30Nfa1GXyKxxBtnIn52g4WNYD71SGzwY"
        )
        assertFalse(isValid)
    }
}
