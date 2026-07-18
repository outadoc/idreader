//
//  SwiftKeyGenerator.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import Shared

class SwiftKeyGenerator: KeyGenerator {
    func generateKeyPair(algorithm: Algorithm) -> KeyPair {
        fatalError()
    }
    
    func generateKeyPairOnGenerator(algorithm: Algorithm, generator: EcPoint) -> KeyPair {
        fatalError()
    }
}
