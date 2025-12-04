package id.walt.webwallet.service.dids

import id.walt.crypto.keys.Key
import id.walt.crypto.keys.KeyManager
import id.walt.did.dids.DidService
import id.walt.webwallet.service.keys.KeysService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object WalletDidKeyResolver {

    suspend fun resolve(wallet: Uuid, did: String): Key =
        DidService.resolveToKey(did).getOrElse { cause ->
            val walletDid = DidsService.get(wallet, did)
                ?: throw cause

            val walletKey = KeysService.get(wallet, walletDid.keyId)
                ?: throw cause

            KeyManager.resolveSerializedKey(walletKey.document)
        }
}

