package com.example.demo_boostar.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter

import com.example.demo_boostar.ui.navigation.Destinations

@Composable
fun PantallaHome(innerPadding: PaddingValues, navigateTo: (Destinations) -> Unit){

    val scrollState = rememberScrollState()

    val fotos: List<String> = mutableListOf(
        "https://i.imgur.com/QkIa5tT.jpeg",
        "https://i.imgur.com/1twoaDy.jpeg",
        "https://i.imgur.com/cHddUCu.jpeg",
        "https://i.imgur.com/cSytoSD.jpeg",
        "https://i.imgur.com/ZKGofuB.jpeg",
        "https://i.imgur.com/mp3rUty.jpeg",
        "https://i.imgur.com/9LFjwpI.jpeg"
    )
    val video = "https://ik.imagekit.io/na4r0zhhy/Video%20promocional%20_%20MODA%20Campa%C3%B1a%20AW%2022%20_%20Scotta%201985%20_%20DOSIS%20VIDEOMARKETING.mp4?updatedAt=1762428334007"

    Column(
        Modifier
            .fillMaxWidth()
            .padding(innerPadding)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),) {
        Text(
            text = "HOME",
            style = TextStyle(
                fontSize = 36.sp
            )
        )
        VideoHeaderExoPlayer(videoUrl = video)
        Section("Para Ti", fotos = fotos) { navigateTo(Destinations.PantallaParaTi) }
        Section("Tendencias", fotos) { navigateTo(Destinations.PantallaTendencias) }
        Section("Productos", fotos) { navigateTo(Destinations.PantallaTendencias) }
        Section("Rebajas", fotos) { navigateTo(Destinations.PantallaTendencias) }
        Section("Marcas", fotos) { navigateTo(Destinations.PantallaTendencias) }
    }
}

@Composable
fun Section(nombre: String, fotos: List<String>, navigateTo: () -> Unit){
    HorizontalDivider(thickness = 2.dp)
    Text(
        text = nombre,
        modifier = Modifier
            .clickable(
                onClick = navigateTo
            )
            .fillMaxWidth(),
        style = TextStyle(
            textAlign = TextAlign.Left,
            fontSize = 24.sp
        )
    )
    LazyRow() {
        items(fotos){
            Card(modifier = Modifier.padding(6.dp)) {
                ItemProducto(it)
            }
        }
    }
}
@Composable
fun VideoHeaderExoPlayer(videoUrl: String) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            prepare()
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
@Composable
fun ItemProducto(URL: String){
    val painter = rememberAsyncImagePainter(
        model = URL,
    )

    Card(modifier = Modifier.size(150.dp)){
        Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier.fillMaxSize()
        )
    }
}
