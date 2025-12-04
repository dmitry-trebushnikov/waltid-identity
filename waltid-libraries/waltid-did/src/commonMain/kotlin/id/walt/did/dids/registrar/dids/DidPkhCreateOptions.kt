package id.walt.did.dids.registrar.dids

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class DidPkhCreateOptions(
    namespace: String = "eip155",
    reference: String = "1",
) : DidCreateOptions(
    method = "pkh",
    config = config(
        "namespace" to namespace,
        "reference" to reference,
    )
)

