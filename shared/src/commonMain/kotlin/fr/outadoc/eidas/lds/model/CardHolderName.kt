package fr.outadoc.eidas.lds.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class CardHolderName(
    val surname: String,
    val givenNames: ImmutableList<String>,
)
