package ggack.newsclip.data.newsapi

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(val retrofitService: NewsInterface)
