//
//  DomainParameterExt.swift
//  iosApp
//
//  Created by Baptiste CANDELLIER on 18/07/2026.
//

import Shared
import SwiftECC

extension DomainParameter {
    // Matched on parameterId (a plain Kotlin Int) rather than the enum
    // case itself, since Swift's spelling of an interop enum case with
    // custom members isn't guaranteed stable across Kotlin/Native versions.
    var ecDomain: Domain {
        switch parameterId {
        case 8: return Domain.instance(curve: .EC192r1)
        case 9: return Domain.instance(curve: .BP192r1)
        case 10: return Domain.instance(curve: .EC224r1)
        case 11: return Domain.instance(curve: .BP224r1)
        case 12: return Domain.instance(curve: .EC256r1)
        case 13: return Domain.instance(curve: .BP256r1)
        case 14: return Domain.instance(curve: .BP320r1)
        case 15: return Domain.instance(curve: .EC384r1)
        case 16: return Domain.instance(curve: .BP384r1)
        case 17: return Domain.instance(curve: .BP512r1)
        case 18: return Domain.instance(curve: .EC521r1)
        default: fatalError("Parameter not available: \(self)")
        }
    }
}
