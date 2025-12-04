package registrars

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.registrar.dids.DidPkhCreateOptions
import id.walt.did.dids.registrar.local.pkh.DidPkhRegistrar
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class DidPkhRegistrarTest : DidRegistrarTestBase(DidPkhRegistrar()) {

    @Test
    fun `register did pkh without key`() {
        `given did options with no key when register then returns a valid did result`(
            DidPkhCreateOptions(reference = "137")
        ) { result, _ ->
            assertTrue(result.did.startsWith("did:pkh:eip155:137:0x"))
            val doc = result.didDocument.toJsonObject()
            assertTrue(doc["verificationMethod"]!!.jsonArray.isNotEmpty())
        }
    }

    @Test
    fun `register did pkh with provided key`() {
        val key = runBlocking { JWKKey.generate(KeyType.secp256k1) }
        `given did options and key when register with key then returns a valid did result`(
            key,
            DidPkhCreateOptions(namespace = "eip155", reference = "80002")
        ) { result, _, _ ->
            assertTrue(result.did.startsWith("did:pkh:eip155:80002:0x"))
        }
    }
}

