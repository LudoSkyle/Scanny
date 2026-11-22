package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class BarcodeContentParserTest {

    @Test
    fun `a wifi payload gives its network name and password`() {
        // Given
        val rawValue = "WIFI:T:WPA;S:Scanny;P:secret;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.WPA,
                password = "secret",
            ),
            actual = content,
        )
    }

    @Test
    fun `escaped characters of a wifi payload are decoded`() {
        // Given
        val rawValue = """WIFI:T:WPA;S:My\;Net;P:pa\:ss\,w\\rd\";;"""

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "My;Net",
                encryption = WifiEncryption.WPA,
                password = "pa:ss,w\\rd\"",
            ),
            actual = content,
        )
    }

    @Test
    fun `a quoted wifi network name and password lose their quotes`() {
        // Given
        val rawValue = """WIFI:S:"Scanny";T:WPA;P:"secret";;"""

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.WPA,
                password = "secret",
            ),
            actual = content,
        )
    }

    @Test
    fun `escaped quotes stay in the wifi network name`() {
        // Given
        val rawValue = """WIFI:S:\"Scanny\";T:nopass;;"""

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "\"Scanny\"",
                encryption = WifiEncryption.NONE,
            ),
            actual = content,
        )
    }

    @Test
    fun `an open wifi network has no encryption and no password`() {
        // Given
        val rawValue = "WIFI:T:nopass;S:Guest;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Guest",
                encryption = WifiEncryption.NONE,
            ),
            actual = content,
        )
    }

    @Test
    fun `a wifi payload without type and without password is open`() {
        // Given
        val rawValue = "WIFI:S:Guest;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Guest",
                encryption = WifiEncryption.NONE,
            ),
            actual = content,
        )
    }

    @Test
    fun `a wifi payload without type but with a password is protected`() {
        // Given
        val rawValue = "WIFI:S:Home;P:secret;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Home",
                encryption = WifiEncryption.WPA,
                password = "secret",
            ),
            actual = content,
        )
    }

    @Test
    fun `a wep network is recognized`() {
        // Given
        val rawValue = "WIFI:T:WEP;S:Old;P:12345;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Old",
                encryption = WifiEncryption.WEP,
                password = "12345",
            ),
            actual = content,
        )
    }

    @Test
    fun `a sae network is a wpa3 network`() {
        // Given
        val rawValue = "WIFI:T:SAE;S:Modern;P:secret;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Modern",
                encryption = WifiEncryption.WPA3,
                password = "secret",
            ),
            actual = content,
        )
    }

    @Test
    fun `a hidden wifi network is flagged`() {
        // Given
        val rawValue = "WIFI:T:WPA;S:Hidden;P:secret;H:true;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Hidden",
                encryption = WifiEncryption.WPA,
                password = "secret",
                isHidden = true,
            ),
            actual = content,
        )
    }

    @Test
    fun `the wifi prefix and keys are case insensitive and the values are not`() {
        // Given
        val rawValue = "wifi:s:Home;t:wpa;p:SeCrEt;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Home",
                encryption = WifiEncryption.WPA,
                password = "SeCrEt",
            ),
            actual = content,
        )
    }

    @Test
    fun `the fields of a wifi payload can come in any order`() {
        // Given
        val rawValue = "WIFI:P:secret;S:Home;T:WPA;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Home",
                encryption = WifiEncryption.WPA,
                password = "secret",
            ),
            actual = content,
        )
    }

    @Test
    fun `a wifi payload without network name is shown as text`() {
        // Given
        val rawValue = "WIFI:T:WPA;P:secret;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `a mecard gives the details of the contact`() {
        // Given
        val rawValue = "MECARD:N:Doe,John;TEL:+33600000000;TEL:+33100000000;EMAIL:john@doe.fr;" +
            "ADR:12 rue de la Paix;URL:https://doe.fr;ORG:Scanny;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe",
                organization = "Scanny",
                phones = listOf(
                    "+33600000000",
                    "+33100000000",
                ),
                emails = listOf("john@doe.fr"),
                addresses = listOf("12 rue de la Paix"),
                urls = listOf("https://doe.fr"),
            ),
            actual = content,
        )
    }

    @Test
    fun `a mecard name without comma is a formatted name`() {
        // Given
        val rawValue = "MECARD:N:John Doe;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(formattedName = "John Doe"),
            actual = content,
        )
    }

    @Test
    fun `an empty mecard is shown as text`() {
        // Given
        val rawValue = "MECARD:;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `a vcard gives the details of the contact`() {
        // Given
        val rawValue = listOf(
            "BEGIN:VCARD",
            "VERSION:3.0",
            "N:Doe;John;;;",
            "FN:John Doe",
            "ORG:Scanny",
            "TITLE:Developer",
            "TEL;TYPE=CELL:+33600000000",
            "TEL;TYPE=WORK:+33100000000",
            "EMAIL;TYPE=INTERNET:john@doe.fr",
            "ADR;TYPE=HOME:;;12 rue de la Paix;Paris;;75002;France",
            "URL:https://doe.fr",
            "END:VCARD",
        ).joinToString(separator = "\n")

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = johnDoeVcardContact(),
            actual = content,
        )
    }

    @Test
    fun `the lines of a vcard can end with crlf`() {
        // Given
        val rawValue = listOf(
            "BEGIN:VCARD",
            "VERSION:3.0",
            "N:Doe;John;;;",
            "FN:John Doe",
            "ORG:Scanny",
            "TITLE:Developer",
            "TEL;TYPE=CELL:+33600000000",
            "TEL;TYPE=WORK:+33100000000",
            "EMAIL;TYPE=INTERNET:john@doe.fr",
            "ADR;TYPE=HOME:;;12 rue de la Paix;Paris;;75002;France",
            "URL:https://doe.fr",
            "END:VCARD",
        ).joinToString(separator = "\r\n")

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = johnDoeVcardContact(),
            actual = content,
        )
    }

    @Test
    fun `a folded vcard line is unfolded`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:3.0\nFN:John D\n oe\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(formattedName = "John Doe"),
            actual = content,
        )
    }

    @Test
    fun `vcard groups and parameters are ignored`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:2.1\nitem1.EMAIL;type=INTERNET:john@doe.fr\n" +
            "TEL;CELL:+33600000000\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                phones = listOf("+33600000000"),
                emails = listOf("john@doe.fr"),
            ),
            actual = content,
        )
    }

    @Test
    fun `escaped characters of a vcard are decoded`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:3.0\nORG:Doe\\, Smith & Co\nTITLE:Line 1\\nLine 2\\; and \\\\\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                organization = "Doe, Smith & Co",
                title = "Line 1\nLine 2; and \\",
            ),
            actual = content,
        )
    }

    @Test
    fun `a vcard 4 telephone uri loses its scheme`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:4.0\nTEL;VALUE=uri;TYPE=cell:tel:+33600000000\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(phones = listOf("+33600000000")),
            actual = content,
        )
    }

    @Test
    fun `an empty vcard is shown as text`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:3.0\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `a mailto link gives the address subject and body`() {
        // Given
        val rawValue = "mailto:john@doe.fr?subject=Hello%20Scanny&body=Line%201%0ALine%202"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                address = "john@doe.fr",
                subject = "Hello Scanny",
                body = "Line 1\nLine 2",
            ),
            actual = content,
        )
    }

    @Test
    fun `a plus sign stays a plus sign in a mailto link`() {
        // Given
        val rawValue = "mailto:john+scan@doe.fr?subject=1+1"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                address = "john+scan@doe.fr",
                subject = "1+1",
            ),
            actual = content,
        )
    }

    @Test
    fun `a mailto link without address keeps its subject`() {
        // Given
        val rawValue = "mailto:?subject=Hello"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(subject = "Hello"),
            actual = content,
        )
    }

    @Test
    fun `a malformed percent sequence stays as typed`() {
        // Given
        val rawValue = "mailto:john@doe.fr?subject=100%25%20sure%2&body=50%"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                address = "john@doe.fr",
                subject = "100% sure%2",
                body = "50%",
            ),
            actual = content,
        )
    }

    @Test
    fun `percent encoded utf8 characters are decoded`() {
        // Given
        val rawValue = "mailto:john@doe.fr?subject=%C3%89t%C3%A9"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                address = "john@doe.fr",
                subject = "Été",
            ),
            actual = content,
        )
    }

    @Test
    fun `the mailto prefix is case insensitive`() {
        // Given
        val rawValue = "MAILTO:john@doe.fr"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(address = "john@doe.fr"),
            actual = content,
        )
    }

    @Test
    fun `a matmsg gives the address subject and body`() {
        // Given
        val rawValue = "MATMSG:TO:john@doe.fr;SUB:Hello;BODY:See you: soon;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                address = "john@doe.fr",
                subject = "Hello",
                body = "See you: soon",
            ),
            actual = content,
        )
    }

    @Test
    fun `an smsto payload gives the number and the message`() {
        // Given
        val rawValue = "SMSTO:+33600000000:Meet at 10:30"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Meet at 10:30",
            ),
            actual = content,
        )
    }

    @Test
    fun `an smsto payload without message has only a number`() {
        // Given
        val rawValue = "SMSTO:+33600000000"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Sms(phoneNumber = "+33600000000"),
            actual = content,
        )
    }

    @Test
    fun `an sms uri gives the number and the body`() {
        // Given
        val rawValue = "sms:+33600000000?body=Hello%20there"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Hello there",
            ),
            actual = content,
        )
    }

    @Test
    fun `an sms uri without body has only a number`() {
        // Given
        val rawValue = "sms:+33600000000"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Sms(phoneNumber = "+33600000000"),
            actual = content,
        )
    }

    @Test
    fun `an https link is an url`() {
        // Given
        val rawValue = "https://scanny.app/path?query=1"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Url(url = rawValue),
            actual = content,
        )
    }

    @Test
    fun `the scheme of an url is case insensitive`() {
        // Given
        val rawValue = "HTTP://SCANNY.APP"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Url(url = rawValue),
            actual = content,
        )
    }

    @Test
    fun `a link without scheme stays text`() {
        // Given
        val rawValue = "www.scanny.app"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `plain text stays text`() {
        // Given
        val rawValue = "Hello Scanny"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `surrounding whitespace does not prevent the recognition`() {
        // Given
        val rawValue = "  https://scanny.app\n"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Url(url = "https://scanny.app"),
            actual = content,
        )
    }

    @Test
    fun `a tel link gives the phone number`() {
        // Given
        val rawValue = "tel:+33600000000"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Phone(phoneNumber = "+33600000000"),
            actual = content,
        )
    }

    @Test
    fun `a percent encoded tel link is decoded and its prefix is case insensitive`() {
        // Given
        val rawValue = "TEL:+33%206%2000%2000%2000%2000"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00"),
            actual = content,
        )
    }

    @Test
    fun `a tel link without number is shown as text`() {
        // Given
        val rawValue = "tel:"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "tel:"),
            actual = content,
        )
    }

    @Test
    fun `a geo link gives the latitude and the longitude`() {
        // Given
        val rawValue = "geo:48.8584,2.2945"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
            ),
            actual = content,
        )
    }

    @Test
    fun `the altitude and the parameters of a geo link are left out`() {
        // Given
        val rawValue = "GEO:-33.8568,151.2153,40;u=35"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = -33.8568,
                longitude = 151.2153,
            ),
            actual = content,
        )
    }

    @Test
    fun `the place in the query of a geo link gives its name`() {
        // Given
        val rawValue = "geo:48.8584,2.2945?q=48.8584,2.2945(Tour%20Eiffel)"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            actual = content,
        )
    }

    @Test
    fun `a geo link at zero takes the coordinates of the place in its query`() {
        // Given
        val rawValue = "geo:0,0?q=48.8584,2.2945(Tour Eiffel)"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            actual = content,
        )
    }

    @Test
    fun `a geo link searching around its position names it after the search`() {
        // Given
        val rawValue = "geo:48.8584,2.2945?q=Tour%20Eiffel"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            actual = content,
        )
    }

    @Test
    fun `a geo link searching an address is shown as text`() {
        // Given
        val rawValue = "geo:0,0?q=1 rue de la Paix, Paris"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "geo:0,0?q=1 rue de la Paix, Paris"),
            actual = content,
        )
    }

    @Test
    fun `a geo link out of the map is shown as text`() {
        // Given
        val rawValue = "geo:91,0"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "geo:91,0"),
            actual = content,
        )
    }

    @Test
    fun `a mebkm payload gives the title and the link`() {
        // Given
        val rawValue = "MEBKM:TITLE:Scanny\\; the app;URL:https\\://scanny.app;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Bookmark(
                url = "https://scanny.app",
                title = "Scanny; the app",
            ),
            actual = content,
        )
    }

    @Test
    fun `a mebkm payload without link is shown as text`() {
        // Given
        val rawValue = "MEBKM:TITLE:Scanny;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "MEBKM:TITLE:Scanny;;"),
            actual = content,
        )
    }

    @Test
    fun `malformed payloads never throw`() {
        // Given
        val random = Random(seed = 42)
        val rawValues = listOf(
            "",
            " ",
            "\\",
            ":::",
            ";;;",
            "WIFI:",
            "WIFI:S:",
            "WIFI:S:\\",
            "WIFI:S:\"",
            "MECARD:",
            "MECARD:N:,",
            "MATMSG:",
            "BEGIN:VCARD",
            "BEGIN:VCARD\nFN",
            "BEGIN:VCARD\n FN:folded first line",
            "BEGIN:VCARD\nN:;;;;;;;;\nADR:;;;;;;;;;",
            "mailto:",
            "mailto:%",
            "mailto:?",
            "mailto:?&=&subject",
            "SMSTO:",
            "SMSTO::",
            "sms:",
            "sms:?body=%E",
            "http://",
            "tel:%",
            "geo:",
            "geo:,",
            "geo:1",
            "geo:NaN,Infinity",
            "geo:0,0?q=",
            "geo:0,0?q=1,2(",
            "MEBKM:",
            "MEBKM:URL:;",
            CharArray(size = 10_000) {
                random.nextInt(
                    from = 0,
                    until = 0x3000,
                ).toChar()
            }.concatToString(),
        )

        // When
        val contents = rawValues.map { rawValue ->
            parse(rawValue = rawValue)
        }

        // Then
        assertEquals(
            expected = rawValues.size,
            actual = contents.size,
        )
    }

    private fun parse(rawValue: String): BarcodeContent =
        BarcodeContentParser.parse(rawValue = rawValue)

    private fun johnDoeVcardContact(): BarcodeContent.Contact =
        BarcodeContent.Contact(
            formattedName = "John Doe",
            firstName = "John",
            lastName = "Doe",
            organization = "Scanny",
            title = "Developer",
            phones = listOf(
                "+33600000000",
                "+33100000000",
            ),
            emails = listOf("john@doe.fr"),
            addresses = listOf("12 rue de la Paix, Paris, 75002, France"),
            urls = listOf("https://doe.fr"),
        )
}