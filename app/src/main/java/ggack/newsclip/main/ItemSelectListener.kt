package ggack.newsclip.main

interface ItemSelectListener {
    fun onSelected(data : Any?)
    fun onSwipe(position : Int, data : Any?)
}