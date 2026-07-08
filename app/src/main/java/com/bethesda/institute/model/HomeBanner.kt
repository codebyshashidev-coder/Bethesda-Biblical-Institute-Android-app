package com.bethesda.institute.model

data class HomeBanner(
    val title: String,
    val subtitle: String,
    val button1Text: String,
    val button1Link: String,
    val button2Text: String,
    val button2Link: String,
    val image: String?
)

data class Testimonial(
    val name: String,
    val role: String,
    val quote: String,
    val rating: Int,
    val photo: String?
)
