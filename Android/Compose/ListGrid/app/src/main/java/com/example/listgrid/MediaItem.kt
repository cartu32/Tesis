package com.example.listgrid

import java.lang.reflect.Type

data class MediaItem(
    val id:Int,
    val title:String,
    val thumb:String,
    val type: Type //Define si tenemos una foto o un video
){
    enum class  Type{PHOTO, VIDEO}
}

//nos da un listado de elementos de preuba, nos da 10 elementos del
//tipo MediaItem
fun getMedia() = (1..10).map{
    MediaItem(
        id=it, //it es el arguemento que nos llega pro map
        title = "Title $it",
        thumb = "https://loremflickr.com/400/400/people?$it",
        //Cada 3 elementos del listado pogo un video
        type = if(it % 3 ==0) MediaItem.Type.VIDEO else MediaItem.Type.PHOTO
    )
}