package fr.outadoc.eidas.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val AppIcons.passport: ImageVector
    get() {
        if (_passport != null) {
            return _passport!!
        }
        _passport =
            ImageVector
                .Builder(
                    name = "passport",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(8.5f, 18.5f)
                        horizontalLineToRelative(7f)
                        verticalLineTo(17f)
                        horizontalLineToRelative(-7f)
                        verticalLineToRelative(1.5f)
                        close()
                        moveToRelative(3.5f, -3f)
                        quadToRelative(2.08f, 0f, 3.54f, -1.46f)
                        reflectiveQuadTo(17f, 10.5f)
                        quadTo(17f, 8.42f, 15.54f, 6.96f)
                        reflectiveQuadTo(12f, 5.5f)
                        quadTo(9.93f, 5.5f, 8.46f, 6.96f)
                        reflectiveQuadTo(7f, 10.5f)
                        reflectiveQuadToRelative(1.46f, 3.54f)
                        reflectiveQuadTo(12f, 15.5f)
                        close()
                        moveToRelative(0f, -1.68f)
                        quadTo(11.8f, 13.55f, 11.58f, 12.91f)
                        quadToRelative(-0.22f, -0.64f, -0.3f, -1.66f)
                        horizontalLineToRelative(1.45f)
                        quadToRelative(-0.08f, 1.02f, -0.3f, 1.66f)
                        reflectiveQuadTo(12f, 13.83f)
                        close()
                        moveTo(10.2f, 13.5f)
                        quadTo(9.6f, 13.13f, 9.18f, 12.55f)
                        quadTo(8.75f, 11.98f, 8.6f, 11.25f)
                        horizontalLineTo(9.78f)
                        quadToRelative(0.05f, 0.63f, 0.15f, 1.19f)
                        reflectiveQuadTo(10.2f, 13.5f)
                        close()
                        moveToRelative(3.6f, 0f)
                        quadTo(13.98f, 13f, 14.08f, 12.44f)
                        reflectiveQuadToRelative(0.15f, -1.19f)
                        horizontalLineTo(15.4f)
                        quadToRelative(-0.15f, 0.72f, -0.57f, 1.3f)
                        quadTo(14.4f, 13.13f, 13.8f, 13.5f)
                        close()
                        moveTo(8.6f, 9.75f)
                        quadTo(8.75f, 9.02f, 9.18f, 8.45f)
                        reflectiveQuadTo(10.2f, 7.5f)
                        quadTo(10.03f, 8f, 9.93f, 8.56f)
                        reflectiveQuadTo(9.78f, 9.75f)
                        horizontalLineTo(8.6f)
                        close()
                        moveToRelative(2.68f, 0f)
                        quadToRelative(0.08f, -1.03f, 0.3f, -1.66f)
                        reflectiveQuadTo(12f, 7.18f)
                        quadToRelative(0.2f, 0.27f, 0.43f, 0.91f)
                        quadToRelative(0.22f, 0.64f, 0.3f, 1.66f)
                        horizontalLineTo(11.28f)
                        close()
                        moveToRelative(2.95f, 0f)
                        quadTo(14.18f, 9.13f, 14.08f, 8.56f)
                        reflectiveQuadTo(13.8f, 7.5f)
                        quadToRelative(0.6f, 0.38f, 1.03f, 0.95f)
                        reflectiveQuadToRelative(0.57f, 1.3f)
                        horizontalLineTo(14.23f)
                        close()
                        moveTo(4f, 22f)
                        verticalLineTo(2f)
                        horizontalLineTo(18f)
                        quadToRelative(0.82f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(20f, 4f)
                        verticalLineTo(20f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(18f, 22f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(6f, 20f)
                        horizontalLineTo(18f)
                        verticalLineTo(4f)
                        horizontalLineTo(6f)
                        verticalLineTo(20f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineTo(4f)
                        verticalLineTo(20f)
                        close()
                    }
                }.build()
        return _passport!!
    }

private var _passport: ImageVector? = null
