//
//  SwiftSettingsEncryptor.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import CryptoKit
import Foundation
import Security
import Shared

class SwiftSettingsEncryptor: SettingsEncryptor {
    // Reuses AndroidSettingsEncryptor's key alias for traceability; the key
    // itself is never shared across platforms, each side just needs to be
    // internally self-consistent for its own encrypt/decrypt round trip.
    private let keychainService = "fr.outadoc.eidas.settings"
    private let keychainAccount = "0f457983-cf72-49f8-9c57-4e76436de169"

    func encrypt(clearText: String) -> KmpResult<NSString> {
        do {
            let key = try loadOrCreateKey()
            let sealedBox = try AES.GCM.seal(Data(clearText.utf8), using: key)
            guard let combined = sealedBox.combined else {
                throw SettingsEncryptorError.sealedBoxMissingCombinedRepresentation
            }
            let cipherText = combined.base64EncodedString() as NSString
            let result: KmpResult<NSString> = KmpResult.companion.success(value: cipherText)
            return result
        } catch {
            let result: KmpResult<NSString> = KmpResult.companion.failure(exception: KotlinThrowable(message: "\(error)"))
            return result
        }
    }

    func decrypt(cipherText: String) -> KmpResult<NSString> {
        do {
            guard let combined = Data(base64Encoded: cipherText) else {
                throw SettingsEncryptorError.invalidBase64CipherText
            }
            let key = try loadOrCreateKey()
            let sealedBox = try AES.GCM.SealedBox(combined: combined)
            let decrypted = try AES.GCM.open(sealedBox, using: key)
            guard let clearText = String(data: decrypted, encoding: .utf8) else {
                throw SettingsEncryptorError.decryptedDataNotUtf8
            }
            let result: KmpResult<NSString> = KmpResult.companion.success(value: clearText as NSString)
            return result
        } catch {
            let result: KmpResult<NSString> = KmpResult.companion.failure(exception: KotlinThrowable(message: "\(error)"))
            return result
        }
    }

    // MARK: - Key storage (Keychain, analogous to AndroidSettingsEncryptor's Keystore alias)

    private func loadOrCreateKey() throws -> SymmetricKey {
        if let existing = try loadKeyData() {
            return SymmetricKey(data: existing)
        }
        let key = SymmetricKey(size: .bits256)
        try storeKeyData(key.withUnsafeBytes { Data($0) })
        return key
    }

    private func loadKeyData() throws -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: keychainService,
            kSecAttrAccount as String: keychainAccount,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        switch status {
        case errSecSuccess:
            return result as? Data
        case errSecItemNotFound:
            return nil
        default:
            throw SettingsEncryptorError.keychain(status)
        }
    }

    private func storeKeyData(_ data: Data) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: keychainService,
            kSecAttrAccount as String: keychainAccount,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
        ]

        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else {
            throw SettingsEncryptorError.keychain(status)
        }
    }
}

private enum SettingsEncryptorError: Error {
    case keychain(OSStatus)
    case invalidBase64CipherText
    case sealedBoxMissingCombinedRepresentation
    case decryptedDataNotUtf8
}
