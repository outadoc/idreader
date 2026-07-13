package fr.outadoc.eidas.screen.picture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import fr.outadoc.eidas.icons.AppIcons
import fr.outadoc.eidas.icons.arrowBack
import fr.outadoc.eidas.lds.model.DocumentPicture
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun FullScreenPictureScreen(
    modifier: Modifier = Modifier,
    documentPicture: DocumentPicture,
    onBack: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.arrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
            )
        },
    ) { insets ->
        Box(modifier = Modifier.padding(insets)) {
            val zoomState = rememberZoomState()
            AsyncImage(
                modifier =
                    Modifier
                        .zoomable(zoomState)
                        .fillMaxSize(),
                contentScale = ContentScale.Fit,
                model = documentPicture,
                contentDescription = "Photo of document holder",
                onSuccess = { state ->
                    zoomState.setContentSize(state.painter.intrinsicSize)
                },
            )
        }
    }
}
