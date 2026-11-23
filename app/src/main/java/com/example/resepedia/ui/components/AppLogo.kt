package com.example.resepedia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.resepedia.R

/**
 * Komponen Reusable Logo Resepedia (Lingkaran Oranye Penuh + Ikon MenuBook Putih)
 * Menggunakan satu sumber vektor resmi: res/drawable/ic_logo_resepedia.xml
 */
@Composable
fun AppLogo(
    size: Dp = 42.dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Image(
        painter = painterResource(id = R.drawable.ic_logo_resepedia),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
