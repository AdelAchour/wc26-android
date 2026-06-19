package com.adel.wc26.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val StylusNote: ImageVector
  get() {
    if (_stylus_note != null) {
      return _stylus_note!!
    }
    _stylus_note =
      ImageVector.Builder(
          name = "stylus_note",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
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
            moveTo(12.48f, 16.83f)
            lineTo(20.85f, 8.45f)
            lineToRelative(-1.3f, -1.3f)
            lineToRelative(-8.38f, 8.38f)
            lineToRelative(1.3f, 1.3f)
            close()
            moveTo(5.95f, 19f)
            quadTo(3.45f, 18.88f, 2.23f, 17.95f)
            reflectiveQuadTo(1f, 15.28f)
            quadTo(1f, 13.65f, 2.34f, 12.64f)
            reflectiveQuadTo(6.05f, 11.43f)
            quadTo(7.03f, 11.35f, 7.51f, 11.11f)
            quadTo(8f, 10.88f, 8f, 10.45f)
            quadTo(8f, 9.8f, 7.26f, 9.48f)
            reflectiveQuadTo(4.83f, 9f)
            lineTo(5f, 7f)
            quadTo(7.58f, 7.2f, 8.79f, 8.04f)
            reflectiveQuadTo(10f, 10.45f)
            quadToRelative(0f, 1.32f, -0.96f, 2.07f)
            quadTo(8.08f, 13.27f, 6.2f, 13.43f)
            quadTo(4.6f, 13.55f, 3.8f, 14.01f)
            reflectiveQuadTo(3f, 15.28f)
            quadToRelative(0f, 0.87f, 0.7f, 1.26f)
            reflectiveQuadTo(6.05f, 17f)
            lineToRelative(-0.1f, 2f)
            close()
            moveToRelative(7f, 0.18f)
            lineTo(8.83f, 15.05f)
            lineTo(18.38f, 5.5f)
            quadTo(18.88f, 5f, 19.56f, 5f)
            reflectiveQuadToRelative(1.19f, 0.5f)
            lineTo(22.5f, 7.25f)
            quadTo(23f, 7.75f, 23f, 8.44f)
            reflectiveQuadTo(22.5f, 9.63f)
            lineToRelative(-9.55f, 9.55f)
            close()
            moveTo(8.98f, 20f)
            quadTo(8.55f, 20.1f, 8.23f, 19.77f)
            reflectiveQuadTo(8f, 19.02f)
            lineTo(8.83f, 15.05f)
            lineToRelative(4.13f, 4.13f)
            lineTo(8.98f, 20f)
            close()
          }
        }
        .build()
    return _stylus_note!!
  }

private var _stylus_note: ImageVector? = null
