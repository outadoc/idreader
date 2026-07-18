//
//  ECFieldElement.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import BigInt
import SwiftECC

extension Domain {
    // The fixed byte width of a coordinate on this curve's prime field,
    // matching BouncyCastle's ECFieldElement.encoded zero-padding behavior.
    var fieldByteSize: Int {
        (p.bitWidth + 7) / 8
    }
}

extension BInt {
    func fieldEncoded(fieldSize: Int) -> [UInt8] {
        let bytes = asMagnitudeBytes()
        if bytes.count < fieldSize {
            return [UInt8](repeating: 0, count: fieldSize - bytes.count) + bytes
        }
        return bytes
    }
}
