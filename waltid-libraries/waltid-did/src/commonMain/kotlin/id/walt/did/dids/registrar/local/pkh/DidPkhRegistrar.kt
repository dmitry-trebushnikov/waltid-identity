package id.walt.did.dids.registrar.local.pkh

import id.walt.crypto.keys.Key
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.Base64Utils.decodeFromBase64Url
import id.walt.did.dids.document.DidDocument
import id.walt.did.dids.document.DidPkhDocument
import id.walt.did.dids.registrar.DidResult
import id.walt.did.dids.registrar.dids.DidCreateOptions
import id.walt.did.dids.registrar.dids.DidPkhCreateOptions
import id.walt.did.dids.registrar.local.LocalRegistrarMethod
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kotlincrypto.hash.sha3.SHA3_256
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class DidPkhRegistrar : LocalRegistrarMethod("pkh") {

    override suspend fun register(options: DidCreateOptions): DidResult =
        registerByKey(JWKKey.generate(KeyType.secp256k1), options)

    override suspend fun registerByKey(key: Key, options: DidCreateOptions): DidResult {
        require(key.keyType == KeyType.secp256k1) { "did:pkh requires a secp256k1 key." }

        val pkhOptions = when (options) {
            is DidPkhCreateOptions -> options
            else -> DidPkhCreateOptions(
                namespace = options["namespace"] ?: "eip155",
                reference = options["reference"] ?: "1",
            )
        }
        val namespace = pkhOptions["namespace"] ?: "eip155"
        val reference = pkhOptions["reference"] ?: "1"

        val publicKey = key.getPublicKey()
        val publicKeyJwk = publicKey.exportJWKObject()
        val accountAddress = deriveEthereumAddress(publicKeyJwk)
        val blockchainAccountId = "$namespace:$reference:$accountAddress"
        val did = "did:pkh:$blockchainAccountId"
        val verificationId = "$did#blockchainAccount"

        val didDocument = DidDocument(
            DidPkhDocument.create(
                did = did,
                blockchainAccountId = blockchainAccountId,
                verificationId = verificationId,
                publicKeyJwk = publicKeyJwk
            ).toMap()
        )

        return DidResult(did = did, didDocument = didDocument)
    }

    private fun deriveEthereumAddress(publicKeyJwk: JsonObject): String {
        val x = publicKeyJwk["x"]?.jsonPrimitive?.content
            ?: error("Missing x coordinate on secp256k1 key")
        val y = publicKeyJwk["y"]?.jsonPrimitive?.content
            ?: error("Missing y coordinate on secp256k1 key")

        val xBytes = x.decodeFromBase64Url()
        val yBytes = y.decodeFromBase64Url()
        val concatenated = ByteArray(xBytes.size + yBytes.size).apply {
            xBytes.copyInto(this, destinationOffset = 0)
            yBytes.copyInto(this, destinationOffset = xBytes.size)
        }

        val keccak = SHA3_256().digest(concatenated)
        val addressBytes = keccak.copyOfRange(keccak.size - ETH_ADDRESS_BYTE_LENGTH, keccak.size)

        return "0x" + addressBytes.joinToString("") { byte ->
            (byte.toInt() and 0xff).toString(16).padStart(2, '0')
        }
    }

    companion object {
        private const val ETH_ADDRESS_BYTE_LENGTH = 20
    }
}

