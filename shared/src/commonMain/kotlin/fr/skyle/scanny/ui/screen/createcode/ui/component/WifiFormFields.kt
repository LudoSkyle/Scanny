package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.anonymous_identity
import fr.skyle.scanny.resources.eap_method
import fr.skyle.scanny.resources.enhanced_open_only
import fr.skyle.scanny.resources.hidden_network
import fr.skyle.scanny.resources.identity
import fr.skyle.scanny.resources.network
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.password_identifier
import fr.skyle.scanny.resources.phase2_method
import fr.skyle.scanny.resources.sae_pk_public_key
import fr.skyle.scanny.resources.security
import fr.skyle.scanny.resources.wifi_security_none
import fr.skyle.scanny.resources.wpa3_enterprise_only
import fr.skyle.scanny.resources.wpa3_only
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.WIFI_EAP_METHODS
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.WIFI_PHASE2_METHODS
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.isPasswordEap
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.isTunneledEap
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import org.jetbrains.compose.resources.stringResource

/**
 * The fields of a Wi-Fi network: those of the "WIFI:" format of the WPA3 specification for a network
 * with a password (the WPA3 only mode, the identifier of the password, the public key of SAE-PK), and
 * those of ZXing for an enterprise network (the method of EAP, the identities, the phase 2).
 */
@Composable
fun WifiFormFields(
    form: CreationForm,
    onFormChanged: (CreationForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    val encryption = form.wifiEncryption

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.wifiSsid,
            onValueChange = { ssid -> onFormChanged(form.copy(wifiSsid = ssid)) },
            label = stringResource(Res.string.network),
            isRequired = true,
        )
        SCDropdownField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.security),
            placeholder = stringResource(Res.string.security),
            options = WifiEncryption.entries,
            selectedOption = encryption,
            optionText = { option -> stringResource(option.toText()) },
            onOptionSelected = { option -> onFormChanged(form.copy(wifiEncryption = option)) },
            style = SCTextFieldDefaults.Style.LIGHT,
        )

        if (encryption == WifiEncryption.WPA_ENTERPRISE) {
            SCDropdownField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.eap_method),
                placeholder = stringResource(Res.string.eap_method),
                options = WIFI_EAP_METHODS,
                selectedOption = form.wifiEapMethod,
                optionText = { method -> method },
                onOptionSelected = { method -> onFormChanged(form.copy(wifiEapMethod = method)) },
                style = SCTextFieldDefaults.Style.LIGHT,
            )
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.wifiIdentity,
                onValueChange = { identity -> onFormChanged(form.copy(wifiIdentity = identity)) },
                label = stringResource(Res.string.identity),
                isRequired = true,
                keyboardType = KeyboardType.Email,
            )
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.wifiAnonymousIdentity,
                onValueChange = { identity -> onFormChanged(form.copy(wifiAnonymousIdentity = identity)) },
                label = stringResource(Res.string.anonymous_identity),
                keyboardType = KeyboardType.Email,
            )
        }

        // A certificate or a SIM card stands for the password of some methods of EAP
        val hasPassword = when (encryption) {
            WifiEncryption.NONE -> false
            WifiEncryption.WEP, WifiEncryption.WPA -> true
            WifiEncryption.WPA_ENTERPRISE -> isPasswordEap(method = form.wifiEapMethod)
        }
        if (hasPassword) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.wifiPassword,
                onValueChange = { password -> onFormChanged(form.copy(wifiPassword = password)) },
                label = stringResource(Res.string.password),
                isRequired = true,
                keyboardType = KeyboardType.Password,
            )
        }

        when (encryption) {
            WifiEncryption.NONE -> CreationSwitchRow(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.enhanced_open_only),
                isChecked = form.isWifiEnhancedOpenOnly,
                onCheckedChange = { isChecked -> onFormChanged(form.copy(isWifiEnhancedOpenOnly = isChecked)) },
            )

            WifiEncryption.WEP -> Unit

            WifiEncryption.WPA -> {
                CreationSwitchRow(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.wpa3_only),
                    isChecked = form.isWifiWpa3Only,
                    onCheckedChange = { isChecked -> onFormChanged(form.copy(isWifiWpa3Only = isChecked)) },
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.wifiPasswordIdentifier,
                    onValueChange = { identifier -> onFormChanged(form.copy(wifiPasswordIdentifier = identifier)) },
                    label = stringResource(Res.string.password_identifier),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.wifiPublicKey,
                    onValueChange = { publicKey -> onFormChanged(form.copy(wifiPublicKey = publicKey)) },
                    label = stringResource(Res.string.sae_pk_public_key),
                    keyboardType = KeyboardType.Password,
                )
            }

            WifiEncryption.WPA_ENTERPRISE -> {
                if (isTunneledEap(method = form.wifiEapMethod)) {
                    SCDropdownField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.phase2_method),
                        placeholder = stringResource(Res.string.phase2_method),
                        options = WIFI_PHASE2_METHODS,
                        selectedOption = form.wifiPhase2Method,
                        optionText = { method -> method ?: stringResource(Res.string.wifi_security_none) },
                        onOptionSelected = { method -> onFormChanged(form.copy(wifiPhase2Method = method)) },
                        style = SCTextFieldDefaults.Style.LIGHT,
                    )
                }
                CreationSwitchRow(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.wpa3_enterprise_only),
                    isChecked = form.isWifiWpa3EnterpriseOnly,
                    onCheckedChange = { isChecked -> onFormChanged(form.copy(isWifiWpa3EnterpriseOnly = isChecked)) },
                )
            }
        }

        CreationSwitchRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.hidden_network),
            isChecked = form.isWifiHidden,
            onCheckedChange = { isHidden -> onFormChanged(form.copy(isWifiHidden = isHidden)) },
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewWifiFormFields() {
    SCTheme {
        WifiFormFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            form = CreationForm(
                kind = CreationKind.WIFI,
                wifiSsid = "Corp",
                wifiEncryption = WifiEncryption.WPA_ENTERPRISE,
            ),
            onFormChanged = {},
        )
    }
}
