package com.bethesda.institute.model

data class NewsItem(
    val id: Int,
    val title: String,
    val slug: String,
    val category: String,
    val excerpt: String?,
    val image: String?,
    val isFeatured: Boolean,
    val createdAt: String
)
