//
//  SwiftCryptoEngine.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import Shared

class SwiftCryptoEngine: CryptoEngine {
    func computeCmac(algorithm: Algorithm, key: KmpBytes, data: KmpBytes) -> KmpBytes {
        fatalError()
    }
    
    func computeMappedGenerator(algorithm: Algorithm, mappingPrivateKey: any PrivateKey, chipMappingPublicPoint: EcPoint, decryptedNonce: KmpBytes) -> EcPoint {
        fatalError()
    }
    
    func computeSha1(message: KmpBytes) -> KmpBytes {
        fatalError()
    }
    
    func computeSharedSecret(algorithm: Algorithm, privateKey: any PrivateKey, chipPublicPoint: EcPoint) -> KmpBytes {
        fatalError()
    }
    
    func decryptSymmetric(algorithm: Algorithm, key: KmpBytes, data: KmpBytes) -> KmpBytes {
        fatalError()
    }
    
    func decryptSymmetricWithIv(algorithm: Algorithm, key: KmpBytes, iv: KmpBytes, data: KmpBytes) -> KmpBytes {
        fatalError()
    }
    
    func deriveKeyFromSecret(algorithm: Algorithm, secret: KmpBytes, nonce: KmpBytes, counter: Int32) -> KmpBytes {
        fatalError()
    }
    
    func encryptSymmetric(algorithm: Algorithm, key: KmpBytes, iv: KmpBytes, data: KmpBytes) -> KmpBytes {
        fatalError()
    }
}
