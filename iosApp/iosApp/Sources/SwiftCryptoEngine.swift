//
//  SwiftCryptoEngine.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import BigInt
import CommonCrypto
import CryptoKit
import Foundation
import Shared
import SwiftECC

class SwiftCryptoEngine: CryptoEngine {
    // Only PACE-ECDH-GM-AES-CBC-CMAC-256 is implemented, matching AndroidCryptoEngine.
    private let supportedProtocolOid = "0.4.0.127.0.7.2.2.4.2.4"

    func computeMappedGenerator(algorithm: Algorithm, mappingPrivateKey: any PrivateKey, chipMappingPublicPoint: EcPoint, decryptedNonce: KmpBytes) -> EcPoint {
        requireSupportedProtocol(algorithm)

        let domain = algorithm.parameter.ecDomain
        let fieldSize = domain.fieldByteSize

        let d = BInt(magnitude: mappingPrivateKey.encoded.bytes)
        let chipPub = Point(
            BInt(magnitude: chipMappingPublicPoint.x.bytes),
            BInt(magnitude: chipMappingPublicPoint.y.bytes)
        )

        let h = try! domain.multiplyPoint(chipPub, d)
        let s = BInt(magnitude: decryptedNonce.bytes).mod(domain.order)
        let sG = try! domain.multiplyPoint(domain.g, s)
        let gPrime = try! domain.addPoints(h, sG)

        return EcPoint(
            x: KmpBytes(bytes: gPrime.x.fieldEncoded(fieldSize: fieldSize)),
            y: KmpBytes(bytes: gPrime.y.fieldEncoded(fieldSize: fieldSize))
        )
    }

    func computeSharedSecret(algorithm: Algorithm, privateKey: any PrivateKey, chipPublicPoint: EcPoint) -> KmpBytes {
        requireSupportedProtocol(algorithm)

        let domain = algorithm.parameter.ecDomain
        let fieldSize = domain.fieldByteSize

        let d = BInt(magnitude: privateKey.encoded.bytes)
        let chipPub = Point(
            BInt(magnitude: chipPublicPoint.x.bytes),
            BInt(magnitude: chipPublicPoint.y.bytes)
        )

        let shared = try! domain.multiplyPoint(chipPub, d)
        return KmpBytes(bytes: shared.x.fieldEncoded(fieldSize: fieldSize))
    }

    func computeCmac(algorithm: Algorithm, key: KmpBytes, data: KmpBytes) -> KmpBytes {
        requireSupportedProtocol(algorithm)
        return KmpBytes(bytes: computeCmacAes(key: key.bytes, data: data.bytes))
    }

    func computeSha1(message: KmpBytes) -> KmpBytes {
        KmpBytes(bytes: Array(Insecure.SHA1.hash(data: Data(message.bytes))))
    }

    func decryptSymmetric(algorithm: Algorithm, key: KmpBytes, data: KmpBytes) -> KmpBytes {
        requireSupportedProtocol(algorithm)
        return KmpBytes(
            bytes: aesCbc(
                encrypt: false,
                key: key.bytes,
                iv: [UInt8](repeating: 0, count: kCCBlockSizeAES128),
                data: data.bytes
            )
        )
    }

    func decryptSymmetricWithIv(algorithm: Algorithm, key: KmpBytes, iv: KmpBytes, data: KmpBytes) -> KmpBytes {
        requireSupportedProtocol(algorithm)
        return KmpBytes(bytes: aesCbc(encrypt: false, key: key.bytes, iv: iv.bytes, data: data.bytes))
    }

    func deriveKeyFromSecret(algorithm: Algorithm, secret: KmpBytes, nonce: KmpBytes, counter: Int32) -> KmpBytes {
        requireSupportedProtocol(algorithm)
        let message = secret.bytes + nonce.bytes + counterBytesBe(counter)
        return KmpBytes(bytes: Array(SHA256.hash(data: Data(message))))
    }

    func encryptSymmetric(algorithm: Algorithm, key: KmpBytes, iv: KmpBytes, data: KmpBytes) -> KmpBytes {
        requireSupportedProtocol(algorithm)
        return KmpBytes(bytes: aesCbc(encrypt: true, key: key.bytes, iv: iv.bytes, data: data.bytes))
    }

    private func requireSupportedProtocol(_ algorithm: Algorithm) {
        guard algorithm.protocol.oid == supportedProtocolOid else {
            fatalError("Unsupported protocol: \(algorithm.protocol.oid)")
        }
    }

    private func counterBytesBe(_ counter: Int32) -> [UInt8] {
        let u = UInt32(bitPattern: counter)
        return [
            UInt8((u >> 24) & 0xff),
            UInt8((u >> 16) & 0xff),
            UInt8((u >> 8) & 0xff),
            UInt8(u & 0xff),
        ]
    }

    // MARK: - AES-CBC (CommonCrypto), no padding — callers apply ISO padding themselves

    private func aesCbc(encrypt: Bool, key: [UInt8], iv: [UInt8], data: [UInt8]) -> [UInt8] {
        var outBytes = [UInt8](repeating: 0, count: data.count + kCCBlockSizeAES128)
        var outLength = 0
        let status = CCCrypt(
            CCOperation(encrypt ? kCCEncrypt : kCCDecrypt),
            CCAlgorithm(kCCAlgorithmAES),
            CCOptions(0),
            key, key.count,
            iv,
            data, data.count,
            &outBytes, outBytes.count,
            &outLength
        )
        precondition(status == kCCSuccess, "AES-CBC operation failed with status \(status)")
        return Array(outBytes.prefix(outLength))
    }

    // MARK: - AES-CMAC (RFC 4493), built on a single-block AES-ECB primitive

    private func aesEcbEncryptBlock(key: [UInt8], block: [UInt8]) -> [UInt8] {
        var outBytes = [UInt8](repeating: 0, count: block.count + kCCBlockSizeAES128)
        var outLength = 0
        let status = CCCrypt(
            CCOperation(kCCEncrypt),
            CCAlgorithm(kCCAlgorithmAES),
            CCOptions(kCCOptionECBMode),
            key, key.count,
            nil,
            block, block.count,
            &outBytes, outBytes.count,
            &outLength
        )
        precondition(status == kCCSuccess, "AES-ECB operation failed with status \(status)")
        return Array(outBytes.prefix(outLength))
    }

    private func xorBytes(_ a: [UInt8], _ b: [UInt8]) -> [UInt8] {
        (0..<a.count).map { a[$0] ^ b[$0] }
    }

    private func leftShiftOneBit(_ input: [UInt8]) -> [UInt8] {
        var output = [UInt8](repeating: 0, count: input.count)
        var carry: UInt8 = 0
        for i in stride(from: input.count - 1, through: 0, by: -1) {
            output[i] = (input[i] << 1) | carry
            carry = (input[i] & 0x80) != 0 ? 1 : 0
        }
        return output
    }

    private func generateCmacSubkeys(key: [UInt8]) -> (k1: [UInt8], k2: [UInt8]) {
        let blockSize = kCCBlockSizeAES128
        var rb = [UInt8](repeating: 0, count: blockSize)
        rb[blockSize - 1] = 0x87

        let l = aesEcbEncryptBlock(key: key, block: [UInt8](repeating: 0, count: blockSize))

        var k1 = leftShiftOneBit(l)
        if (l[0] & 0x80) != 0 {
            k1 = xorBytes(k1, rb)
        }

        var k2 = leftShiftOneBit(k1)
        if (k1[0] & 0x80) != 0 {
            k2 = xorBytes(k2, rb)
        }

        return (k1, k2)
    }

    private func computeCmacAes(key: [UInt8], data: [UInt8]) -> [UInt8] {
        let blockSize = kCCBlockSizeAES128
        let (k1, k2) = generateCmacSubkeys(key: key)

        let blockCount = data.isEmpty ? 1 : (data.count + blockSize - 1) / blockSize
        let lastBlockComplete = !data.isEmpty && data.count % blockSize == 0

        let mLast: [UInt8]
        if lastBlockComplete {
            let lastBlock = Array(data[(blockCount - 1) * blockSize..<data.count])
            mLast = xorBytes(lastBlock, k1)
        } else {
            var lastBlock = Array(data[(blockCount - 1) * blockSize..<data.count])
            lastBlock.append(0x80)
            while lastBlock.count < blockSize {
                lastBlock.append(0)
            }
            mLast = xorBytes(lastBlock, k2)
        }

        var x = [UInt8](repeating: 0, count: blockSize)
        for i in 0..<(blockCount - 1) {
            let block = Array(data[(i * blockSize)..<((i + 1) * blockSize)])
            x = aesEcbEncryptBlock(key: key, block: xorBytes(x, block))
        }

        return aesEcbEncryptBlock(key: key, block: xorBytes(mLast, x))
    }
}
