package com.huaguang.flowoftime.test

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.R

@Preview(showBackground = true)
@Composable
fun CoilTest() {

    val context = LocalContext.current

    Column {

        SvgImage(
            context = context,
            url = "https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/icons-svg/3D%E7%9C%BC%E9%95%9C_three-d-glasses.svg",
            modifier = Modifier
        )

        AsyncImage(
            model = "https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-20230824170145212.png",
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-20230827015614507.png")
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = painterResource(id = R.drawable.expand),
            onSuccess = {
                RDALogger.info("加载成功了！")
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        SubcomposeAsyncImage(
            model = "https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image/icons/3D%E7%9C%BC%E9%95%9C_three-d-glasses.webp",
            contentDescription = null,
            loading = {
                CircularProgressIndicator() // 圆形进度条
            },
            modifier = Modifier.size(48.dp)
        )
        
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image-2023180128735.png")
                .error(R.drawable.explore)
                .size(200, 200)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth() // 图片的大小是由修饰符的 size 决定的，不是由 ImageRequest.Builder 的 size 决定的
        ) {
            if (painter.state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator()
            } else {
                SubcomposeAsyncImageContent()
            }
        }


    }
}

@Composable
fun SvgImage(
    context: Context,
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = null,
        modifier = modifier
    )
}