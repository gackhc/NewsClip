package ggack.newsclip.main

interface ItemSelectListener {
    fun onSelected(position : Int)
    fun onSwipe(position : Int, data : Any?)
}