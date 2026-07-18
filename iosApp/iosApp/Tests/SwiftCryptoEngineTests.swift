//
//  SwiftCryptoEngineTests.swift
//  iosAppTests
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//
//  Mirrors shared/src/androidHostTest/kotlin/fr/outadoc/eidas/crypto/AndroidCryptoEngineTest.kt
//  using the same real-card-captured test vectors, to validate SwiftCryptoEngine's
//  deriveKeyFromSecret and computeCmac against Android's known-good output.

import Testing
@testable import IosApp
import Shared

struct SwiftCryptoEngineTests {
    let cryptoEngine = SwiftCryptoEngine()

    // Algorithm(PACE_ECDH_GM_AES_CBC_CMAC_256, BRAINPOOLP256R1), taken from the
    // shared preferredAlgorithms list to avoid guessing Kotlin enum case names.
    let algorithm = Algorithm.companion.preferredAlgorithms[0]

    let sharedSecret = hexDecoded("24B98B1CAB1BA8987E36F0FB14AB117D102A9CC94CA0A4B9DE03E6E76DB5DAFE")
    let expectedKEnc = hexDecoded("329376CE32D140C51861BDD3878ED0B1823AC9144241975AD8A8D018624F2FBD")
    let expectedKMac = hexDecoded("7CFBDBB1859B666FF20F13A7C17F1850DD4B32871B335D4BDA239C48F5EE52BF")
    let chipFinalPub = hexDecoded("041AAF77535B2CD3D5D87E2A8728823C508895500143173A19990F50EDA5B3C2D10BBB64BB26EE00E4916C362FC156DF8ADCB14386AA2CEAE88B450E6A919970AB")
    let expectedTerminalToken = hexDecoded("86201849E47220D4")

    // DER content of OID 0.4.0.127.0.7.2.2.4.2.4, hardcoded to avoid depending on
    // Kotlin UByteArray bridging for the Protocol.oidBytes extension property.
    let protocolOid = hexDecoded("04007F00070202040204")

    @Test func kdfWithCounter1ProducesKEnc() {
        let kEnc = cryptoEngine.deriveKeyFromSecret(
            algorithm: algorithm,
            secret: KmpBytes(bytes: sharedSecret),
            nonce: KmpBytes(bytes: []),
            counter: 1
        )
        #expect(kEnc.bytes == expectedKEnc)
    }

    @Test func kdfWithCounter2ProducesKMac() {
        let kMac = cryptoEngine.deriveKeyFromSecret(
            algorithm: algorithm,
            secret: KmpBytes(bytes: sharedSecret),
            nonce: KmpBytes(bytes: []),
            counter: 2
        )
        #expect(kMac.bytes == expectedKMac)
    }

    @Test func cmacOfChipPubUnderKMacMatchesLoggedTerminalToken() {
        let tokenInput = paceTokenInput(oid: protocolOid, pubKey: chipFinalPub)
        let mac = cryptoEngine.computeCmac(
            algorithm: algorithm,
            key: KmpBytes(bytes: expectedKMac),
            data: KmpBytes(bytes: tokenInput)
        )
        #expect(Array(mac.bytes.prefix(8)) == expectedTerminalToken)
    }

    private func paceTokenInput(oid: [UInt8], pubKey: [UInt8]) -> [UInt8] {
        let oidTlv: [UInt8] = [0x06, UInt8(oid.count)] + oid
        let pubKeyTlv: [UInt8] = [0x86, UInt8(pubKey.count)] + pubKey
        let inner = oidTlv + pubKeyTlv
        return [0x7F, 0x49, UInt8(inner.count)] + inner
    }
}

private func hexDecoded(_ hex: String) -> [UInt8] {
    var bytes = [UInt8]()
    var index = hex.startIndex
    while index < hex.endIndex {
        let next = hex.index(index, offsetBy: 2)
        bytes.append(UInt8(hex[index..<next], radix: 16)!)
        index = next
    }
    return bytes
}
