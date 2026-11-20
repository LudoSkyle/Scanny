package fr.skyle.scanny.platform.contact

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.platform.ui.topViewController
import kotlinx.cinterop.BetaInteropApi
import platform.Contacts.CNContact
import platform.Contacts.CNLabelOther
import platform.Contacts.CNLabelPhoneNumberMobile
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNMutableContact
import platform.Contacts.CNMutablePostalAddress
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactViewController
import platform.ContactsUI.CNContactViewControllerDelegateProtocol
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

private fun BarcodeContent.Contact.toCNContact(): CNMutableContact =
    CNMutableContact().apply {
        // Contacts on iOS has no formatted name: without first and last names, it takes their place
        if (firstName == null && lastName == null) {
            setGivenName(formattedName.orEmpty())
        } else {
            setGivenName(firstName.orEmpty())
            setFamilyName(lastName.orEmpty())
        }
        setOrganizationName(organization.orEmpty())
        setJobTitle(title.orEmpty())
        setPhoneNumbers(
            phones.map { phone ->
                CNLabeledValue.labeledValueWithLabel(
                    label = CNLabelPhoneNumberMobile,
                    value = CNPhoneNumber.phoneNumberWithStringValue(phone),
                )
            },
        )
        setEmailAddresses(
            emails.map { email ->
                CNLabeledValue.labeledValueWithLabel(
                    label = CNLabelOther,
                    value = email.toNSString(),
                )
            },
        )
        setPostalAddresses(
            addresses.map { address ->
                CNLabeledValue.labeledValueWithLabel(
                    label = CNLabelOther,
                    value = CNMutablePostalAddress().apply {
                        setStreet(address)
                    },
                )
            },
        )
        setUrlAddresses(
            urls.map { url ->
                CNLabeledValue.labeledValueWithLabel(
                    label = CNLabelOther,
                    value = url.toNSString(),
                )
            },
        )
    }

// Contacts types the values of e-mails and websites as NSString, which a String is not typed as
@OptIn(BetaInteropApi::class)
private fun String.toNSString(): NSString =
    NSString.create(string = this)