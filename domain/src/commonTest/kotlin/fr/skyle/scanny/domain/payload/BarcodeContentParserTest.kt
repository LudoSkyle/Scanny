package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.ContactWebsite
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
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
    fun `a sae network is a wpa3 network only`() {
        // Given
        val rawValue = "WIFI:T:SAE;S:Modern;P:secret;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Modern",
                encryption = WifiEncryption.WPA,
                password = "secret",
                transitionDisable = setOf(WifiTransitionMode.WPA3_PERSONAL),
            ),
            actual = content,
        )
    }

    @Test
    fun `a wpa3 network gives the modes it does not fall back from its password identifier and its public key`() {
        // Given
        val rawValue = "WIFI:T:WPA;R:3;S:MyNet;I:john;P:a2bc-de3f-ghi4;K:MDkwEwYHKoZIzj0CAQ==;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "MyNet",
                encryption = WifiEncryption.WPA,
                password = "a2bc-de3f-ghi4",
                transitionDisable = setOf(WifiTransitionMode.WPA3_PERSONAL, WifiTransitionMode.SAE_PK),
                passwordIdentifier = "john",
                publicKey = "MDkwEwYHKoZIzj0CAQ==",
            ),
            actual = content,
        )
    }

    @Test
    fun `an enterprise network gives its eap method its identities and its phase 2`() {
        // Given
        val rawValue = "WIFI:T:WPA2-EAP;R:4;S:Corp;P:secret;E:peap;A:anon;I:john@corp;PH2:mschapv2;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Corp",
                encryption = WifiEncryption.WPA_ENTERPRISE,
                password = "secret",
                transitionDisable = setOf(WifiTransitionMode.WPA3_ENTERPRISE),
                eapMethod = "PEAP",
                identity = "john@corp",
                anonymousIdentity = "anon",
                phase2Method = "MSCHAPV2",
            ),
            actual = content,
        )
    }

    @Test
    fun `an enhanced open network only does not fall back to an open one`() {
        // Given
        val rawValue = "WIFI:T:nopass;R:8;S:Cafe;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Cafe",
                encryption = WifiEncryption.NONE,
                transitionDisable = setOf(WifiTransitionMode.ENHANCED_OPEN),
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
                    ContactPhone(number = "+33600000000"),
                    ContactPhone(number = "+33100000000"),
                ),
                emails = listOf(ContactEmail(address = "john@doe.fr")),
                addresses = listOf(ContactAddress(street = "12 rue de la Paix")),
                websites = listOf(ContactWebsite(url = "https://doe.fr")),
                format = ContactFormat.MECARD,
            ),
            actual = content,
        )
    }

    @Test
    fun `a mecard gives its nickname its birthday its note its videophone and its addresses in parts`() {
        // Given
        val rawValue = "MECARD:N:Doe,John;NICKNAME:Johnny;BDAY:19900512;NOTE:Hi\\, there;TEL-AV:+33100000000;" +
            "ADR:,Apt 4,12 rue de la Paix,Paris,IDF,75002,France;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe",
                nickname = "Johnny",
                phones = listOf(ContactPhone(number = "+33100000000")),
                addresses = listOf(
                    ContactAddress(
                        extended = "Apt 4",
                        street = "12 rue de la Paix",
                        city = "Paris",
                        region = "IDF",
                        postalCode = "75002",
                        country = "France",
                    ),
                ),
                birthday = "1990-05-12",
                note = "Hi, there",
                format = ContactFormat.MECARD,
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
            expected = BarcodeContent.Contact(
                formattedName = "John Doe",
                format = ContactFormat.MECARD,
            ),
            actual = content,
        )
    }

    @Test
    fun `an escaped comma belongs to the name of a mecard`() {
        // Given
        val rawValue = "MECARD:N:Doe\\, Jr;;"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                formattedName = "Doe, Jr",
                format = ContactFormat.MECARD,
            ),
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
    fun `a vcard gives every part of the name the unit of the organization the birthday and the note`() {
        // Given
        val rawValue = listOf(
            "BEGIN:VCARD",
            "VERSION:3.0",
            "N:Doe;John;Quincy;Dr.;Jr.",
            "NICKNAME:Johnny",
            "ORG:Scanny;Mobile;Android",
            "BDAY:1990-05-12T08:30:00Z",
            "NOTE:Met at the fair\\nCall back",
            "TEL;TYPE=WORK,FAX:+33100000000",
            "TEL;TYPE=HOME:+33200000000",
            "TEL:+33300000000",
            "EMAIL;TYPE=INTERNET,WORK:john@scanny.app",
            "URL;TYPE=HOME:https://doe.fr",
            "END:VCARD",
        ).joinToString(separator = "\n")

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                namePrefix = "Dr.",
                firstName = "John",
                middleName = "Quincy",
                lastName = "Doe",
                nameSuffix = "Jr.",
                nickname = "Johnny",
                organization = "Scanny",
                department = "Mobile, Android",
                phones = listOf(
                    ContactPhone(number = "+33100000000", type = ContactPhoneType.FAX),
                    ContactPhone(number = "+33200000000", type = ContactPhoneType.HOME),
                    ContactPhone(number = "+33300000000"),
                ),
                emails = listOf(ContactEmail(address = "john@scanny.app", type = ContactEntryType.WORK)),
                websites = listOf(ContactWebsite(url = "https://doe.fr", type = ContactEntryType.HOME)),
                birthday = "1990-05-12",
                note = "Met at the fair\nCall back",
            ),
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
    fun `vcard groups are ignored and the types of every version are read`() {
        // Given
        val rawValue = "BEGIN:VCARD\nVERSION:2.1\nitem1.EMAIL;type=INTERNET:john@doe.fr\n" +
            "TEL;CELL:+33600000000\nEND:VCARD"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                phones = listOf(ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE)),
                emails = listOf(ContactEmail(address = "john@doe.fr")),
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
            expected = BarcodeContent.Contact(phones = listOf(ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE))),
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
                to = listOf("john@doe.fr"),
                subject = "Hello Scanny",
                body = "Line 1\nLine 2",
            ),
            actual = content,
        )
    }

    @Test
    fun `a mailto link gives its recipients its copies and its blind copies`() {
        // Given
        val rawValue = "mailto:john@doe.fr,jane%40doe.fr?to=jim@doe.fr&cc=a@doe.fr,%20b@doe.fr&bcc=c@doe.fr&subject=Hello"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                to = listOf("john@doe.fr", "jane@doe.fr", "jim@doe.fr"),
                cc = listOf("a@doe.fr", "b@doe.fr"),
                bcc = listOf("c@doe.fr"),
                subject = "Hello",
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
                to = listOf("john+scan@doe.fr"),
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
                to = listOf("john@doe.fr"),
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
                to = listOf("john@doe.fr"),
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
            expected = BarcodeContent.Email(to = listOf("john@doe.fr")),
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
                to = listOf("john@doe.fr"),
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
    fun `an mmsto payload gives the number and the message`() {
        // Given
        val rawValue = "mmsto:+33600000000:See you"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Mms(
                phoneNumber = "+33600000000",
                message = "See you",
            ),
            actual = content,
        )
    }

    @Test
    fun `an mms uri gives the number the subject and the body`() {
        // Given
        val rawValue = "MMS:+33600000000?subject=Hello&body=See%20you%20at%2010%3A30"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Mms(
                phoneNumber = "+33600000000",
                subject = "Hello",
                message = "See you at 10:30",
            ),
            actual = content,
        )
    }

    @Test
    fun `an empty mms uri is shown as text`() {
        // Given
        val rawValue = "mms:"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "mms:"),
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
    fun `a geo link gives its altitude and its uncertainty`() {
        // Given
        val rawValue = "GEO:-33.8568,151.2153,40;crs=WGS84;u=35"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = -33.8568,
                longitude = 151.2153,
                altitude = 40.0,
                uncertainty = 35.0,
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
    fun `a geo link of another reference system is shown as text`() {
        // Given
        val rawValue = "geo:48.8584,2.2945;crs=moon-2011"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "geo:48.8584,2.2945;crs=moon-2011"),
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
    fun `an otpauth link gives the key its issuer and its account`() {
        // Given
        val rawValue = "otpauth://totp/Scanny:john%40doe.fr?secret=JBSWY3DPEHPK3PXP&issuer=Scanny"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                issuer = "Scanny",
                account = "john@doe.fr",
            ),
            actual = content,
        )
    }

    @Test
    fun `an otpauth link keeps its parameters as read`() {
        // Given
        val rawValue = "OTPAUTH://HOTP/john?secret=JBSWY3DPEHPK3PXP&algorithm=SHA256&digits=8&counter=42&period=60"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                type = OtpType.HOTP,
                account = "john",
                algorithm = OtpAlgorithm.SHA256,
                digits = 8,
                period = 60,
                counter = 42,
            ),
            actual = content,
        )
    }

    @Test
    fun `the issuer of the parameters wins over the one of the label`() {
        // Given
        val rawValue = "otpauth://totp/Old%20name:%20john?secret=JBSWY3DPEHPK3PXP&issuer=New%20name"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                issuer = "New name",
                account = "john",
            ),
            actual = content,
        )
    }

    @Test
    fun `an otpauth link of an unknown algorithm is shown as text`() {
        // Given
        val rawValue = "otpauth://totp/john?secret=JBSWY3DPEHPK3PXP&algorithm=MD5"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = rawValue),
            actual = content,
        )
    }

    @Test
    fun `an otpauth link without secret is shown as text`() {
        // Given
        val rawValue = "otpauth://totp/Scanny:john?issuer=Scanny"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "otpauth://totp/Scanny:john?issuer=Scanny"),
            actual = content,
        )
    }

    @Test
    fun `an otpauth link of an unknown type is shown as text`() {
        // Given
        val rawValue = "otpauth://motp/john?secret=JBSWY3DPEHPK3PXP"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "otpauth://motp/john?secret=JBSWY3DPEHPK3PXP"),
            actual = content,
        )
    }

    @Test
    fun `a bitcoin link gives the address the amount the label and the message`() {
        // Given
        val rawValue = "bitcoin:bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq?amount=0.0015&label=Scanny%20shop&message=Order%2042"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Bitcoin(
                address = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                amount = "0.0015",
                label = "Scanny shop",
                message = "Order 42",
            ),
            actual = content,
        )
    }

    @Test
    fun `a bitcoin link in capitals for a smaller code is recognized`() {
        // Given
        val rawValue = "BITCOIN:BC1QAR0SRRR7XFKVY5L643LYDNW9RE59GTZZWF5MDQ"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Bitcoin(address = "BC1QAR0SRRR7XFKVY5L643LYDNW9RE59GTZZWF5MDQ"),
            actual = content,
        )
    }

    @Test
    fun `an amount that is not a number is left out`() {
        // Given
        val rawValue = "bitcoin:1BoatSLRHtKNngkdXEeobR76b53LETtpyT?amount=a%20lot"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Bitcoin(address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"),
            actual = content,
        )
    }

    @Test
    fun `a bitcoin link without address is shown as text`() {
        // Given
        val rawValue = "bitcoin:?amount=1"

        // When
        val content = parse(rawValue = rawValue)

        // Then
        assertEquals(
            expected = BarcodeContent.Text(text = "bitcoin:?amount=1"),
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
            "WIFI:R:zz;S:a;;",
            "WIFI:T:WPA2-EAP;S:a;I:;E:;;",
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
            "MMSTO:",
            "MMSTO::",
            "mms:?subject=%",
            "MECARD:N:\\",
            "MECARD:N:a\\,b,c\\",
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
            "otpauth://",
            "otpauth://totp",
            "otpauth://totp/?secret=",
            "otpauth://totp/%?secret=A&digits=x&counter=99999999999999999999",
            "bitcoin:",
            "bitcoin:?",
            "bitcoin:a?amount=%",
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
                ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE),
                ContactPhone(number = "+33100000000", type = ContactPhoneType.WORK),
            ),
            emails = listOf(ContactEmail(address = "john@doe.fr")),
            addresses = listOf(
                ContactAddress(
                    street = "12 rue de la Paix",
                    city = "Paris",
                    postalCode = "75002",
                    country = "France",
                    type = ContactEntryType.HOME,
                ),
            ),
            websites = listOf(ContactWebsite(url = "https://doe.fr")),
        )
}