package id.walt.did.dids.document

import id.walt.did.dids.DidUtils
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@OptIn(ExperimentalSerializationApi::class, ExperimentalJsExport::class)
@Serializable
data class DidPkhDocument(
    @EncodeDefault
    @SerialName("@context")
    val context: List<String> = DidUtils.DEFAULT_CONTEXT + "https://w3id.org/security/suites/secp256k1-2019/v1",
    val id: String,
    val verificationMethod: List<VerificationMethod>,
    val authentication: List<String>,
    val assertionMethod: List<String>,
    val capabilityInvocation: List<String>,
    val capabilityDelegation: List<String>,
    val keyAgreement: List<String>,
) {

    @Serializable
    data class VerificationMethod(
        val id: String,
        val type: String = "EcdsaSecp256k1RecoveryMethod2020",
        val controller: String,
        val blockchainAccountId: String,
        val publicKeyJwk: JsonObject? = null,
    )

    fun toMap(): Map<String, kotlinx.serialization.json.JsonElement> =
        Json.encodeToJsonElement(this).jsonObject.toMap()

    companion object {
        fun create(
            did: String,
            blockchainAccountId: String,
            verificationId: String,
            publicKeyJwk: JsonObject? = null,
        ) = DidPkhDocument(
            id = did,
            verificationMethod = listOf(
                VerificationMethod(
                    id = verificationId,
                    controller = did,
                    blockchainAccountId = blockchainAccountId,
                    publicKeyJwk = publicKeyJwk,
                )
            ),
            authentication = listOf(verificationId),
            assertionMethod = listOf(verificationId),
            capabilityInvocation = listOf(verificationId),
            capabilityDelegation = listOf(verificationId),
            keyAgreement = listOf(verificationId),
        )
    }
}

