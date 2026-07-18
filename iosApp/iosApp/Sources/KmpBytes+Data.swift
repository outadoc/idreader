//
//  KmpBytes+Data.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import Shared

extension KmpBytes {
    var bytes: [UInt8] {
        let count = Int(raw.size)
        return (0..<count).map { UInt8(bitPattern: raw.get(index: Int32($0))) }
    }

    convenience init(bytes: [UInt8]) {
        let byteArray = KotlinByteArray(size: Int32(bytes.count))
        for (index, byte) in bytes.enumerated() {
            byteArray.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        self.init(raw: byteArray)
    }
}
