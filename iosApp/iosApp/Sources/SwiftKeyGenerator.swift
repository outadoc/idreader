//
//  SwiftKeyGenerator.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import BigInt
import Shared
import SwiftECC

class SwiftPrivateKey: PrivateKey {
    let scalar: BInt

    init(scalar: BInt) {
        self.scalar = scalar
    }

    var encoded: KmpBytes {
        KmpBytes(bytes: scalar.asMagnitudeBytes())
    }
}

class SwiftPublicKey: PublicKey {
    private let point: EcPoint

    init(point: EcPoint) {
        self.point = point
    }

    var encoded: KmpBytes {
        uncompressedPublicPoint
    }

    var uncompressedPublicPoint: KmpBytes {
        KmpBytes(bytes: [0x04] + point.x.bytes + point.y.bytes)
    }
}

class SwiftKeyGenerator: KeyGenerator {
    func generateKeyPair(algorithm: Algorithm) -> KeyPair {
        let domain = algorithm.parameter.ecDomain
        return makeKeyPair(on: domain.g, domain: domain)
    }

    func generateKeyPairOnGenerator(algorithm: Algorithm, generator: EcPoint) -> KeyPair {
        let domain = algorithm.parameter.ecDomain
        let g = Point(
            BInt(magnitude: generator.x.bytes),
            BInt(magnitude: generator.y.bytes)
        )
        return makeKeyPair(on: g, domain: domain)
    }

    private func makeKeyPair(on generator: Point, domain: Domain) -> KeyPair {
        let fieldSize = domain.fieldByteSize
        let d = (domain.order - BInt.ONE).randomLessThan() + BInt.ONE
        let pub = try! domain.multiplyPoint(generator, d)

        return KeyPair(
            privateKey: SwiftPrivateKey(scalar: d),
            publicKey: SwiftPublicKey(
                point: EcPoint(
                    x: KmpBytes(bytes: pub.x.fieldEncoded(fieldSize: fieldSize)),
                    y: KmpBytes(bytes: pub.y.fieldEncoded(fieldSize: fieldSize))
                )
            )
        )
    }
}
