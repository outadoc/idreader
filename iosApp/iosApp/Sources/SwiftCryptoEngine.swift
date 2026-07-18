//
//  SwiftCryptoEngine.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import Shared

class SwiftCryptoEngine: CryptoEngine {
    func computeCmac(algorithm: Algorithm, key: Any, data: Any) -> Any {
        fatalError()
    }
    
    func computeMappedGenerator(algorithm: Algorithm, mappingPrivateKey: any PrivateKey, chipMappingPublicPoint: EcPoint, decryptedNonce: Any) -> EcPoint {
        fatalError()
    }
    
    func computeSha1(message: Any) -> Any {
        fatalError()
    }
    
    func computeSharedSecret(algorithm: Algorithm, privateKey: any PrivateKey, chipPublicPoint: EcPoint) -> Any {
        fatalError()
    }
    
    func decryptSymmetric(algorithm: Algorithm, key: Any, data: Any) -> Any {
        fatalError()
    }
    
    func decryptSymmetricWithIv(algorithm: Algorithm, key: Any, iv: Any, data: Any) -> Any {
        fatalError()
    }
    
    func deriveKeyFromSecret(algorithm: Algorithm, secret: Any, nonce: Any, counter: Int32) -> Any {
        fatalError()
    }
    
    func encryptSymmetric(algorithm: Algorithm, key: Any, iv: Any, data: Any) -> Any {
        fatalError()
    }
}
