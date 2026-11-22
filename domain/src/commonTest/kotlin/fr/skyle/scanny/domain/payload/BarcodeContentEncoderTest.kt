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
import kotlin.test.Test
import kotlin.test.assertEquals

class BarcodeContentEncoderTest {

    @Test
    fun `a text is written as is`() {
        // Given
        val content = BarcodeContent.Text(text = "Hello Scanny")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "Hello Scanny",
            actual = payload,
        )
    }

    @Test
    fun `an url is written as is`() {
        // Given
        val content = BarcodeContent.Url(url = "https://scanny.app")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "https://scanny.app",
            actual = payload,
        )
    }

    @Test
    fun `an email is written as a percent encoded mailto link`() {
        // Given
        val content = BarcodeContent.Email(
            to = listOf("john@doe.fr"),
            subject = "Hello Scanny",
            body = "Line 1\nLine 2",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr?subject=Hello%20Scanny&body=Line%201%0ALine%202",
            actual = payload,
        )
    }

    @Test
    fun `an email writes its recipients its copies and its blind copies separated by commas`() {
        // Given
        val content = BarcodeContent.Email(
            to = listOf("john@doe.fr", "jane@doe.fr"),
            cc = listOf("a@doe.fr", "b@doe.fr"),
            bcc = listOf("c@doe.fr"),
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr,jane@doe.fr?cc=a@doe.fr,b@doe.fr&bcc=c@doe.fr",
            actual = payload,
        )
    }

    @Test
    fun `a location writes its altitude and its uncertainty`() {
        // Given
        val content = BarcodeContent.Location(
            latitude = 48.8584,
            longitude = 2.2945,
            altitude = 330.5,
            uncertainty = 35.0,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "geo:48.8584,2.2945,330.5;u=35",
            actual = payload,
        )
    }

    @Test
    fun `an email without subject and body has no query`() {
        // Given
        val content = BarcodeContent.Email(to = listOf("john@doe.fr"))

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr",
            actual = payload,
        )
    }

    @Test
    fun `an sms is written as an smsto payload`() {
        // Given
        val content = BarcodeContent.Sms(
            phoneNumber = "+33600000000",
            message = "Hello",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "SMSTO:+33600000000:Hello",
            actual = payload,
        )
    }

    @Test
    fun `a wifi network is written with its escaped fields`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "My;Net",
            encryption = WifiEncryption.WPA,
            password = "pa:ss",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = """WIFI:T:WPA;S:My\;Net;P:pa\:ss;;""",
            actual = payload,
        )
    }

    @Test
    fun `an open wifi network is written without password`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Guest",
            encryption = WifiEncryption.NONE,
            password = "unused",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "WIFI:T:nopass;S:Guest;;",
            actual = payload,
        )
    }

    @Test
    fun `a hidden wpa3 network is written as a hidden sae network`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Modern",
            encryption = WifiEncryption.WPA3,
            password = "secret",
            isHidden = true,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "WIFI:T:SAE;S:Modern;P:secret;H:true;;",
            actual = payload,
        )
    }

    @Test
    fun `a contact is written as a vcard 3`() {
        // Given
        val content = BarcodeContent.Contact(
            formattedName = "Dr. John Doe",
            namePrefix = "Dr.",
            firstName = "John",
            lastName = "Doe",
            nickname = "Johnny",
            organization = "Doe, Smith & Co",
            department = "Mobile",
            phones = listOf(
                ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE),
                ContactPhone(number = "+33100000000"),
            ),
            emails = listOf(ContactEmail(address = "john@doe.fr", type = ContactEntryType.WORK)),
            addresses = listOf(
                ContactAddress(
                    street = "12 rue de la Paix",
                    city = "Paris",
                    postalCode = "75002",
                    type = ContactEntryType.HOME,
                ),
            ),
            websites = listOf(ContactWebsite(url = "https://doe.fr")),
            birthday = "1990-05-12",
            note = "Line 1\nLine 2",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = listOf(
                "BEGIN:VCARD",
                "VERSION:3.0",
                "N:Doe;John;;Dr.;",
                "FN:Dr. John Doe",
                "NICKNAME:Johnny",
                "ORG:Doe\\, Smith & Co;Mobile",
                "TEL;TYPE=CELL:+33600000000",
                "TEL:+33100000000",
                "EMAIL;TYPE=WORK:john@doe.fr",
                "ADR;TYPE=HOME:;;12 rue de la Paix;Paris;;75002;",
                "URL:https://doe.fr",
                "BDAY:1990-05-12",
                "NOTE:Line 1\\nLine 2",
                "END:VCARD",
            ).joinToString(separator = "\n"),
            actual = payload,
        )
    }

    @Test
    fun `a phone number is written as a percent encoded tel link`() {
        // Given
        val content = BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "tel:+33%206%2000%2000%2000%2000",
            actual = payload,
        )
    }

    @Test
    fun `a location is written as a geo link`() {
        // Given
        val content = BarcodeContent.Location(
            latitude = 48.8584,
            longitude = 2.2945,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "geo:48.8584,2.2945",
            actual = payload,
        )
    }

    @Test
    fun `a location with a name puts its place in the query`() {
        // Given
        val content = BarcodeContent.Location(
            latitude = 48.8584,
            longitude = 2.2945,
            label = "Tour Eiffel",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "geo:48.8584,2.2945?q=48.8584,2.2945(Tour%20Eiffel)",
            actual = payload,
        )
    }

    @Test
    fun `coordinates are written in decimals rounded to seven places`() {
        // Given
        val content = BarcodeContent.Location(
            latitude = 0.00001,
            longitude = -122.00000012345,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "geo:0.00001,-122.0000001",
            actual = payload,
        )
    }

    @Test
    fun `a bookmark is written as a mebkm payload with its escaped fields`() {
        // Given
        val content = BarcodeContent.Bookmark(
            url = "https://scanny.app",
            title = "Scanny; the app",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "MEBKM:TITLE:Scanny\\; the app;URL:https\\://scanny.app;;",
            actual = payload,
        )
    }

    @Test
    fun `an otp key is written as an otpauth link with its issuer in the label and the parameters`() {
        // Given
        val content = BarcodeContent.Otp(
            secret = "JBSWY3DPEHPK3PXP",
            issuer = "Scanny & co",
            account = "john@doe.fr",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "otpauth://totp/Scanny%20%26%20co:john%40doe.fr?secret=JBSWY3DPEHPK3PXP&issuer=Scanny%20%26%20co",
            actual = payload,
        )
    }

    @Test
    fun `an otp key writes only the parameters it was given`() {
        // Given
        val content = BarcodeContent.Otp(
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            account = "john",
            algorithm = OtpAlgorithm.SHA256,
            digits = 8,
            counter = 42,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "otpauth://hotp/john?secret=JBSWY3DPEHPK3PXP&algorithm=SHA256&digits=8&counter=42",
            actual = payload,
        )
    }

    @Test
    fun `a bitcoin payment is written as a bitcoin link of bip 21`() {
        // Given
        val content = BarcodeContent.Bitcoin(
            address = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            amount = "0.0015",
            label = "Scanny shop",
            message = "Order #42",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "bitcoin:bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq?amount=0.0015&label=Scanny%20shop&message=Order%20%2342",
            actual = payload,
        )
    }

    @Test
    fun `an mms is written as an mms link with its subject and its body`() {
        // Given
        val content = BarcodeContent.Mms(
            phoneNumber = "+33600000000",
            subject = "Hello there",
            message = "See you",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mms:+33600000000?subject=Hello%20there&body=See%20you",
            actual = payload,
        )
    }

    @Test
    fun `a contact read in a mecard is written as a mecard with its escaped fields`() {
        // Given
        val content = BarcodeContent.Contact(
            firstName = "John",
            lastName = "Doe; Smith",
            organization = "Scanny",
            phones = listOf(ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE)),
            emails = listOf(ContactEmail(address = "john@doe.fr")),
            addresses = listOf(
                ContactAddress(
                    street = "12 rue de la Paix",
                    city = "Paris",
                ),
            ),
            birthday = "1990-05-12",
            format = ContactFormat.MECARD,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            // A MeCard has no kind of telephone
            expected = "MECARD:N:Doe\\; Smith,John;ORG:Scanny;TEL:+33600000000;EMAIL:john@doe.fr;" +
                "ADR:,,12 rue de la Paix,Paris,,,;BDAY:19900512;;",
            actual = payload,
        )
    }

    @Test
    fun `every written content is read back unchanged`() {
        // Given
        val contents = listOf(
            BarcodeContent.Text(text = "Hello: Scanny; with, special \\ characters\n"),
            BarcodeContent.Url(url = "https://scanny.app/path?query=1&other=été"),
            BarcodeContent.Email(
                to = listOf("john+scan@doe.fr"),
                subject = "100% sure & certain = yes?",
                body = "Été\nà bientôt",
            ),
            BarcodeContent.Email(body = "Only a body"),
            BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Meet at 10:30; okay?",
            ),
            BarcodeContent.Sms(message = "Only a message"),
            BarcodeContent.Wifi(
                ssid = """My;Net,with:all\special"chars""",
                encryption = WifiEncryption.WPA,
                password = """pa;ss,wo:rd\with"quotes""",
            ),
            BarcodeContent.Wifi(
                ssid = "\"Quoted\"",
                encryption = WifiEncryption.WEP,
                password = "\"12345\"",
                isHidden = true,
            ),
            BarcodeContent.Wifi(
                ssid = "Guest",
                encryption = WifiEncryption.NONE,
            ),
            BarcodeContent.Wifi(
                ssid = "Modern",
                encryption = WifiEncryption.WPA3,
                password = "secret",
            ),
            BarcodeContent.Contact(
                formattedName = "John; Doe, Jr\nthe second",
                firstName = "John",
                lastName = "Doe; Smith",
                organization = "Doe, Smith & Co",
                title = "Lead\\developer",
                phones = listOf(
                    ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE),
                    ContactPhone(number = "+33100000000", type = ContactPhoneType.FAX),
                    ContactPhone(number = "+33200000000"),
                ),
                emails = listOf(ContactEmail(address = "john@doe.fr", type = ContactEntryType.HOME)),
                addresses = listOf(
                    ContactAddress(
                        poBox = "BP 12",
                        extended = "Apt 4; building B",
                        street = "12 rue de la Paix, 2nd floor",
                        city = "Paris",
                        region = "Île-de-France",
                        postalCode = "75002",
                        country = "France",
                        type = ContactEntryType.WORK,
                    ),
                ),
                websites = listOf(ContactWebsite(url = "https://doe.fr", type = ContactEntryType.WORK)),
            ),
            BarcodeContent.Contact(
                namePrefix = "Dr.",
                middleName = "Quincy",
                nameSuffix = "Jr.; Sr.",
                nickname = "Johnny, the kid",
                department = "Mobile; Android",
                birthday = "1990-05-12",
                note = "Line 1\nLine 2; with \\ and ,",
            ),
            BarcodeContent.Contact(firstName = "John"),
            BarcodeContent.Phone(phoneNumber = "+33 (0)6 00-00.00 00"),
            BarcodeContent.Location(
                latitude = -33.8568,
                longitude = 151.2152967,
            ),
            BarcodeContent.Location(
                latitude = 27.988,
                longitude = 86.925,
                altitude = -12.5,
                uncertainty = 0.5,
                label = "Everest; or not",
            ),
            BarcodeContent.Email(
                to = listOf("john+1@doe.fr", "jane@doe.fr"),
                cc = listOf("a@doe.fr"),
                bcc = listOf("b@doe.fr", "c@doe.fr"),
                subject = "Hi, all & co",
            ),
            BarcodeContent.Email(cc = listOf("only@copy.fr")),
            BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour (Eiffel) & co, Paris",
            ),
            BarcodeContent.Bookmark(
                url = "https://scanny.app/path?query=1;other=2",
                title = "Scanny: \"the\" app, with\\backslash",
            ),
            BarcodeContent.Bookmark(url = "https://scanny.app"),
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                issuer = "Scanny: the app?",
                account = "john+2fa@doe.fr & co",
            ),
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                type = OtpType.HOTP,
                issuer = "Scanny",
                algorithm = OtpAlgorithm.SHA512,
                digits = 8,
                period = 60,
                counter = 0,
            ),
            BarcodeContent.Otp(secret = "JBSWY3DPEHPK3PXP"),
            BarcodeContent.Bitcoin(
                address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                amount = "12.5",
                label = "Été & co=1?",
                message = "100% sure",
            ),
            BarcodeContent.Bitcoin(address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"),
            BarcodeContent.Mms(
                phoneNumber = "+33 6 00 00 00 00",
                subject = "Hi: there & co",
                message = "See you; 10% off",
            ),
            BarcodeContent.Mms(subject = "Only a subject"),
            BarcodeContent.Contact(
                formattedName = "Doe, Jr: the \"second\"; really",
                nickname = "Johnny; the kid",
                organization = "Doe, Smith & Co",
                phones = listOf(ContactPhone(number = "+33 6 00 00 00 00")),
                emails = listOf(ContactEmail(address = "john@doe.fr")),
                addresses = listOf(
                    ContactAddress(street = "12 rue de la Paix, 75002 Paris"),
                    ContactAddress(
                        extended = "Apt 4",
                        street = "12 rue de la Paix",
                        city = "Paris, center",
                        country = "France",
                    ),
                ),
                websites = listOf(ContactWebsite(url = "https://doe.fr")),
                birthday = "1990-05-12",
                note = "See you; soon, ok: \\",
                format = ContactFormat.MECARD,
            ),
            BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe, Smith",
                title = "Lead\\developer",
                format = ContactFormat.MECARD,
            ),
        )

        // When
        val readContents = contents.map { content ->
            BarcodeContentParser.parse(rawValue = encode(content = content))
        }

        // Then
        assertEquals(
            expected = contents,
            actual = readContents,
        )
    }

    private fun encode(content: BarcodeContent): String =
        BarcodeContentEncoder.encode(content = content)
}