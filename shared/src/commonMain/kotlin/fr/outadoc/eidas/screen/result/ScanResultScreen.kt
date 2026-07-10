package fr.outadoc.eidas.screen.result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.arrowBack
import fr.outadoc.eidas.presentation.CardInfoUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    cardInfo: CardInfoUiModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Scan result") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = AppIcons.arrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { insets ->
        CardInfo(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(insets)
                    .padding(16.dp),
            cardInfo = cardInfo,
        )
    }
}
