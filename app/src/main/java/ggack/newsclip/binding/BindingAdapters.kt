package ggack.newsclip.binding

import android.annotation.SuppressLint
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import coil.load
import ggack.newsclip.R
import ggack.newsclip.data.MultimediaModel
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
@BindingAdapter("setDate")
fun setDate(v : TextView, strDate : String) {
    // "2022-12-30T03:41:58+0000"strDate
    val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ")
    val sdfTarget = SimpleDateFormat("yyyy년 MM월 dd일")
    val date = sdfSource.parse(strDate.replace("T", " "))
    v.text = date?.let { sdfTarget.format(it) }
}


@SuppressLint("UseCompatLoadingForDrawables")
@BindingAdapter("setImgUrl")
fun setImgUrl(imageView : ImageView, model : List<MultimediaModel>?) {
    if(model?.isNotEmpty() == true) {
        model[0].url.let {
            val url = imageView.context.getString(R.string.img_url_base) + it
            val imgUri = url.toUri().buildUpon().scheme("https").build()
            imageView.load(imgUri) {
                placeholder(R.drawable.loading_animation)
                error(R.drawable.ic_broken_image)
            }
        }
    } else {
        imageView.setImageDrawable(imageView.context.getDrawable(R.drawable.ic_broken_image))
    }
}

@BindingAdapter("setWebViewUrl")
fun setWebViewUrl(v : WebView, url : String) {
    v.loadUrl(url)
}