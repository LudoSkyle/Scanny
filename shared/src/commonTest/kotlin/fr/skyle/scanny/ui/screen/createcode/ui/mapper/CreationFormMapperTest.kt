package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactAddressField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactEntryField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactPhoneField
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CreationFormMapperTest {

    @Test
    fun `a blank text is no content yet`() {
        // When
        val content = CreationForm(text = "   ").toBarcodeContent()

        // Then
        assertNull(content)
    }

    @Test
    fun `a link typed without its scheme gets https and one with http keeps it`() {
        // When
        val contents = listOf(" scanny.app ", "http://scanny.app").map { url ->
            CreationForm(
                kind = CreationKind.LINK,
                url = url,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Url(url = "https://scanny.app"),
                BarcodeContent.Url(url = "http://scanny.app"),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an e-mail takes several addresses in each of its recipients copies and blind copies`() {
        // When
        val content = CreationForm(
            kind = CreationKind.EMAIL,
            emailTo = " hello@scanny.app, team@scanny.app ",
            emailCc = "a@scanny.app; b@scanny.app",
            emailBcc = "c@scanny.app",
            emailSubject = " ",
        ).toBarcodeContent()

        // Then
        assertEquals(
            expected = BarcodeContent.Email(
                to = listOf("hello@scanny.app", "team@scanny.app"),
                cc = listOf("a@scanny.app", "b@scanny.app"),
                bcc = listOf("c@scanny.app"),
            ),
            actual = content,
        )
    }

    @Test
    fun `an e-mail needs one field at least but no field in particular`() {
        // When
        val contents = listOf(
            CreationForm(kind = CreationKind.EMAIL),
            CreationForm(
                kind = CreationKind.EMAIL,
                emailSubject = "Hello",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Email(subject = "Hello"),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an sms needs its phone number`() {
        // When
        val contents = listOf("", "+33600000000").map { phoneNumber ->
            CreationForm(
                kind = CreationKind.SMS,
                smsPhoneNumber = phoneNumber,
                smsMessage = "On my way",
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Sms(
                    phoneNumber = "+33600000000",
                    message = "On my way",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a wpa network needs a name and a password of 8 to 63 characters`() {
        // When
        val contents = listOf(
            "Scanny" to "short",
            "" to "long enough",
            "Scanny" to "long enough",
        ).map { (ssid, password) ->
            CreationForm(
                kind = CreationKind.WIFI,
                wifiSsid = ssid,
                wifiEncryption = WifiEncryption.WPA,
                wifiPassword = password,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                null,
                BarcodeContent.Wifi(
                    ssid = "Scanny",
                    encryption = WifiEncryption.WPA,
                    password = "long enough",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an open network keeps no password`() {
        // When
        val content = CreationForm(
            kind = CreationKind.WIFI,
            wifiSsid = "Scanny",
            wifiEncryption = WifiEncryption.NONE,
            wifiPassword = "typed before",
            isWifiHidden = true,
        ).toBarcodeContent()

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.NONE,
                isHidden = true,
            ),
            actual = content,
        )
    }

    @Test
    fun `a vcard contact keeps every field filled in and is shown by the parts of its name`() {
        // When
        val content = CreationForm(
            kind = CreationKind.CONTACT,
            contactNamePrefix = " Dr. ",
            contactFirstName = "John",
            contactMiddleName = "Quincy",
            contactLastName = "Doe",
            contactNameSuffix = "Jr.",
            contactNickname = "Johnny",
            contactOrganization = "Scanny",
            contactDepartment = "Mobile",
            contactTitle = "Lead",
            contactPhones = listOf(
                ContactPhoneField(number = " +33600000000 ", type = ContactPhoneType.MOBILE),
                ContactPhoneField(number = " ", type = ContactPhoneType.WORK),
            ),
            contactEmails = listOf(ContactEntryField(value = "john@scanny.app", type = ContactEntryType.WORK)),
            contactAddresses = listOf(
                ContactAddressField(street = "12 rue de la Paix", postalCode = "75002", city = "Paris", type = ContactEntryType.WORK),
                ContactAddressField(),
            ),
            contactWebsites = listOf(ContactEntryField(value = " ")),
            contactBirthday = "1990-05-12",
            contactNote = "Met at the fair",
        ).toBarcodeContent()

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                formattedName = "Dr. John Quincy Doe Jr.",
                namePrefix = "Dr.",
                firstName = "John",
                middleName = "Quincy",
                lastName = "Doe",
                nameSuffix = "Jr.",
                nickname = "Johnny",
                organization = "Scanny",
                department = "Mobile",
                title = "Lead",
                phones = listOf(ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE)),
                emails = listOf(ContactEmail(address = "john@scanny.app", type = ContactEntryType.WORK)),
                addresses = listOf(
                    ContactAddress(street = "12 rue de la Paix", city = "Paris", postalCode = "75002", type = ContactEntryType.WORK),
                ),
                birthday = "1990-05-12",
                note = "Met at the fair",
            ),
            actual = content,
        )
    }

    @Test
    fun `a vcard contact needs a name shown typed made of its parts or of its organization`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.CONTACT,
                contactPhones = listOf(ContactPhoneField(number = "+33600000000")),
            ),
            CreationForm(
                kind = CreationKind.CONTACT,
                contactFirstName = "John",
                contactDisplayName = " Johnny Doe ",
            ),
            CreationForm(
                kind = CreationKind.CONTACT,
                contactOrganization = "Scanny",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Contact(
                    formattedName = "Johnny Doe",
                    firstName = "John",
                ),
                BarcodeContent.Contact(
                    formattedName = "Scanny",
                    organization = "Scanny",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a mecard contact needs a first or a last name and leaves out what a mecard does not hold`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.CONTACT,
                contactDisplayName = "John Doe",
                contactFormat = ContactFormat.MECARD,
            ),
            CreationForm(
                kind = CreationKind.CONTACT,
                contactNamePrefix = "Dr.",
                contactFirstName = "John",
                contactLastName = "Doe",
                contactDisplayName = "Johnny",
                contactDepartment = "Mobile",
                contactTitle = "Lead",
                contactPhones = listOf(ContactPhoneField(number = "+33600000000", type = ContactPhoneType.MOBILE)),
                contactFormat = ContactFormat.MECARD,
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Contact(
                    firstName = "John",
                    lastName = "Doe",
                    phones = listOf(ContactPhone(number = "+33600000000")),
                    format = ContactFormat.MECARD,
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an mms needs its phone number and leaves out a blank subject`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.MMS,
                mmsSubject = "Hello",
            ),
            CreationForm(
                kind = CreationKind.MMS,
                smsPhoneNumber = " +33600000000 ",
                mmsSubject = " ",
                smsMessage = "See you",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Mms(
                    phoneNumber = "+33600000000",
                    message = "See you",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a contact is written in the format chosen`() {
        // When
        val content = CreationForm(
            kind = CreationKind.CONTACT,
            contactFirstName = "John",
            contactFormat = ContactFormat.MECARD,
        ).toBarcodeContent()

        // Then
        assertEquals(
            expected = BarcodeContent.Contact(
                firstName = "John",
                format = ContactFormat.MECARD,
            ),
            actual = content,
        )
    }

    @Test
    fun `a phone needs its number`() {
        // When
        val contents = listOf("  ", " +33 6 00 00 00 00 ").map { phoneNumber ->
            CreationForm(
                kind = CreationKind.PHONE,
                phoneNumber = phoneNumber,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00"),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a location takes a decimal comma or point and keeps its place if named`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = " 48,8584 ",
                locationLongitude = "2.2945",
                locationLabel = " Tour Eiffel ",
            ),
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = "-33.8568",
                locationLongitude = "151,2153",
                locationLabel = " ",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Location(
                    latitude = 48.8584,
                    longitude = 2.2945,
                    label = "Tour Eiffel",
                ),
                BarcodeContent.Location(
                    latitude = -33.8568,
                    longitude = 151.2153,
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a location takes an altitude and an uncertainty in meters`() {
        // When
        val contents = listOf(
            "-12,5" to "0.5",
            "abc" to "",
            "" to "-1",
        ).map { (altitude, uncertainty) ->
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = "27.988",
                locationLongitude = "86.925",
                locationAltitude = altitude,
                locationUncertainty = uncertainty,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Location(
                    latitude = 27.988,
                    longitude = 86.925,
                    altitude = -12.5,
                    uncertainty = 0.5,
                ),
                null,
                null,
            ),
            actual = contents,
        )
    }

    @Test
    fun `a location needs both coordinates on the map`() {
        // When
        val contents = listOf(
            "48.8584" to "",
            "91" to "2.2945",
            "48.8584" to "-181",
            "48,85,84" to "2.2945",
            "NaN" to "2.2945",
        ).map { (latitude, longitude) ->
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = latitude,
                locationLongitude = longitude,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(null, null, null, null, null),
            actual = contents,
        )
    }

    @Test
    fun `a bookmark needs its link which gets https without its scheme`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.BOOKMARK,
                bookmarkTitle = "Scanny",
            ),
            CreationForm(
                kind = CreationKind.BOOKMARK,
                bookmarkTitle = " Scanny ",
                bookmarkUrl = " scanny.app ",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Bookmark(
                    url = "https://scanny.app",
                    title = "Scanny",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an otp key typed in groups and in lower case is read as base32`() {
        // When
        val content = CreationForm(
            kind = CreationKind.OTP,
            otpIssuer = " Scanny ",
            otpAccount = " john@doe.fr ",
            otpSecret = "jbsw y3dp-ehpk 3pxp",
        ).toBarcodeContent()

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
    fun `an otp key needs an account and 16 base32 characters`() {
        // When
        val contents = listOf(
            "JBSWY3DPEHPK3PX" to "john",
            "JBSWY3DPEHPK3PX1" to "john",
            "JBSWY3DPEHPK3PXP" to " ",
        ).map { (secret, account) ->
            CreationForm(
                kind = CreationKind.OTP,
                otpIssuer = "Scanny",
                otpAccount = account,
                otpSecret = secret,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(null, null, null),
            actual = contents,
        )
    }

    @Test
    fun `a totp key needs a period and a hotp key its counter`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
                otpPeriod = "0",
            ),
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
                otpType = OtpType.HOTP,
            ),
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
                otpType = OtpType.HOTP,
                otpCounter = "-1",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(null, null, null),
            actual = contents,
        )
    }

    @Test
    fun `every parameter of an otp key is written unless left to its default`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
                otpType = OtpType.HOTP,
                otpAlgorithm = OtpAlgorithm.SHA256,
                otpDigits = 8,
                otpPeriod = "60",
                otpCounter = " 3 ",
            ),
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
                otpPeriod = " 60 ",
                otpCounter = "3",
            ),
            CreationForm(
                kind = CreationKind.OTP,
                otpAccount = "john",
                otpSecret = "JBSWY3DPEHPK3PXP",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                // A period is of a TOTP key only
                BarcodeContent.Otp(
                    secret = "JBSWY3DPEHPK3PXP",
                    type = OtpType.HOTP,
                    account = "john",
                    algorithm = OtpAlgorithm.SHA256,
                    digits = 8,
                    counter = 3,
                ),
                // And a counter of a HOTP key only
                BarcodeContent.Otp(
                    secret = "JBSWY3DPEHPK3PXP",
                    account = "john",
                    period = 60,
                ),
                BarcodeContent.Otp(
                    secret = "JBSWY3DPEHPK3PXP",
                    account = "john",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a bitcoin payment needs an address and takes an amount with a decimal comma`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.BITCOIN,
                bitcoinAddress = " 1BoatSLRHtKNngkdXEeobR76b53LETtpyT ",
                bitcoinAmount = " 0,0015 ",
                bitcoinLabel = " Scanny shop ",
                bitcoinMessage = " ",
            ),
            CreationForm(
                kind = CreationKind.BITCOIN,
                bitcoinAddress = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                bitcoinAmount = "0.000000001",
            ),
            CreationForm(
                kind = CreationKind.BITCOIN,
                bitcoinAddress = "not an address",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Bitcoin(
                    address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                    amount = "0.0015",
                    label = "Scanny shop",
                ),
                null,
                null,
            ),
            actual = contents,
        )
    }

    @Test
    fun `a barcode holds its value as a text in the format chosen and the others are qr codes`() {
        // Given
        val barcode = CreationForm(
            kind = CreationKind.BARCODE,
            barcodeFormat = BarcodeFormat.CODE_128,
            barcodeValue = " SCANNY-128 ",
        )

        // Then
        assertEquals(
            expected = Triple(BarcodeContent.Text(text = "SCANNY-128"), BarcodeFormat.CODE_128, BarcodeFormat.QR_CODE),
            actual = Triple(barcode.toBarcodeContent(), barcode.format, CreationForm(kind = CreationKind.WIFI).format),
        )
    }
}