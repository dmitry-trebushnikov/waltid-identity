package id.walt.did.dids.resolver.local

import id.walt.crypto.keys.Key
import id.walt.did.dids.DidUtils
import id.walt.did.dids.document.DidDocument
import id.walt.did.dids.document.DidPkhDocument
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class DidPkhResolver : LocalResolverMethod("pkh") {

    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    override suspend fun resolve(did: String): Result<DidDocument> = runCatching {
        val identifier = DidUtils.identifierFromDid(did)
            ?: throw IllegalArgumentException("Invalid did:pkh identifier: $did")
        val parts = identifier.split(":")
        require(parts.size >= 3) { "did:pkh identifier must include namespace, reference and account: $did" }

        val namespace = parts[0]
        val reference = parts[1]
        val account = parts.drop(2).joinToString(":")
        val blockchainAccountId = "$namespace:$reference:$account"
        val verificationId = "$did#blockchainAccount"

        DidDocument(
            DidPkhDocument.create(
                did = did,
                blockchainAccountId = blockchainAccountId,
                verificationId = verificationId,
                publicKeyJwk = null
            ).toMap()
        )
    }

    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    override suspend fun resolveToKey(did: String): Result<Key> =
        Result.failure(UnsupportedOperationException("did:pkh does not expose a resolvable public key"))
}

