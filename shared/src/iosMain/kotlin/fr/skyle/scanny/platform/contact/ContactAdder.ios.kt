package fr.skyle.scanny.platform.contact

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.platform.ui.topViewController
import kotlinx.cinterop.BetaInteropApi
import platform.Contacts.CNContact
import platform.Contacts.CNLabelHome
import platform.Contacts.CNLabelOther
import platform.Contacts.CNLabelPhoneNumberMobile
import platform.Contacts.CNLabelPhoneNumberOtherFax
import platform.Contacts.CNLabelWork
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNMutableContact
import platform.Contacts.CNMutablePostalAddress
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactViewController
import platform.ContactsUI.CNContactViewControllerDelegateProtocol
import platform.Foundation.NSDateComponents
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UINavigationController
import platform.darwin.NSObject

internal class IosContactAdder : ContactAdder {

    // Kept here: the view controller only holds its delegate weakly
    private val delegate = NewContactDelegate()

    override fun addContact(contact: BarcodeContent.Contact) {
        val contactViewController = CNContactViewController.viewControllerForNewContact(contact.toCNContact())
        contactViewController.delegate = delegate

        // In a navigation controller, for its buttons to cancel and to save
        topViewController()?.presentViewController(
            viewControllerToPresent = UINavigationController(rootViewController = contactViewController),
            animated = true,
            completion = null,
        )
    }
}

private class NewContactDelegate :
    NSObject(),
    CNContactViewControllerDelegateProtocol {

    // Called whether the contact was saved or not: the screen closes either way
    override fun contactViewController(
        viewController: CNContactViewController,
        didCompleteWithContact: CNContact?,
    ) {
        viewController.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )
    }
}

// The note is left out: Contacts lets an app write it only with an entitlement of Apple
private fun BarcodeContent.Contact.toCNContact(): CNMutableContact =
    CNMutableContact().apply {
        // Contacts on iOS has no formatted name: without the parts of a name, it takes their place
        if (listOf(namePrefix, firstName, middleName, lastName, nameSuffix).all { part -> part == null }) {
            setGivenName(formattedName.orEmpty())
        } else {
            setNamePrefix(namePrefix.orEmpty())
            setGivenName(firstName.orEmpty())
            setMiddleName(middleName.orEmpty())
            setFamilyName(lastName.orEmpty())
            setNameSuffix(nameSuffix.orEmpty())
        }
        setNickname(nickname.orEmpty())
        setOrganizationName(organization.orEmpty())
        setDepartmentName(department.orEmpty())
        setJobTitle(title.orEmpty())
        setPhoneNumbers(
            phones.map { phone ->
                CNLabeledValue.labeledValueWithLabel(
                    label = phone.type.toLabel(),
                    value = CNPhoneNumber.phoneNumberWithStringValue(phone.number),
                )
            },
        )
        setEmailAddresses(
            emails.map { email ->
                CNLabeledValue.labeledValueWithLabel(
                    label = email.type.toLabel(),
                    value = email.address.toNSString(),
                )
            },
        )
        setPostalAddresses(
            addresses.map { address ->
                CNLabeledValue.labeledValueWithLabel(
                    label = address.type.toLabel(),
                    value = CNMutablePostalAddress().apply {
                        // Contacts has no post office box nor extended address: they come with the street
                        setStreet(listOfNotNull(address.street, address.extended, address.poBox).joinToString(separator = "\n"))
                        setCity(address.city.orEmpty())
                        setState(address.region.orEmpty())
                        setPostalCode(address.postalCode.orEmpty())
                        setCountry(address.country.orEmpty())
                    },
                )
            },
        )
        setUrlAddresses(
            websites.map { website ->
                CNLabeledValue.labeledValueWithLabel(
                    label = website.type.toLabel(),
                    value = website.url.toNSString(),
                )
            },
        )
        // The birthday of the content, not the one of the contact being built
        this@toCNContact.birthday?.let { date -> setBirthday(date.toDateComponents()) }
    }

private fun ContactPhoneType.toLabel(): String? =
    when (this) {
        ContactPhoneType.MOBILE -> CNLabelPhoneNumberMobile
        ContactPhoneType.HOME -> CNLabelHome
        ContactPhoneType.WORK -> CNLabelWork
        ContactPhoneType.FAX -> CNLabelPhoneNumberOtherFax
        ContactPhoneType.OTHER -> CNLabelOther
    }

private fun ContactEntryType.toLabel(): String? =
    when (this) {
        ContactEntryType.HOME -> CNLabelHome
        ContactEntryType.WORK -> CNLabelWork
        ContactEntryType.OTHER -> CNLabelOther
    }

// "1990-05-12": the day of a birthday, which has no time
private fun String.toDateComponents(): NSDateComponents =
    NSDateComponents().apply {
        val (year, month, day) = split('-').map { part -> part.toLong() }
        setYear(year)
        setMonth(month)
        setDay(day)
    }

// Contacts types the values of e-mails and websites as NSString, which a String is not typed as
@OptIn(BetaInteropApi::class)
private fun String.toNSString(): NSString =
    NSString.create(string = this)