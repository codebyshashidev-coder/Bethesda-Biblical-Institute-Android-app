package com.bethesda.institute.net

import com.bethesda.institute.model.HomeBanner
import com.bethesda.institute.model.Testimonial
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class HomeData(val banner: HomeBanner, val testimonials: List<Testimonial>)

object HomeApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun fetchHome(apiUrl: String, callback: (HomeData?, Exception?) -> Unit) {
        val request = Request.Builder().url(apiUrl).get().build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    try {
                        val body = resp.body?.string() ?: throw IOException("Empty response")
                        val json = JSONObject(body)
                        if (!json.optBoolean("success", false)) {
                            callback(null, IOException(json.optString("error", "Unknown error")))
                            return
                        }

                        val b = json.getJSONObject("banner")
                        val banner = HomeBanner(
                            title = b.optString("title"),
                            subtitle = b.optString("subtitle"),
                            button1Text = b.optString("button1_text"),
                            button1Link = b.optString("button1_link"),
                            button2Text = b.optString("button2_text"),
                            button2Link = b.optString("button2_link"),
                            image = if (b.isNull("image")) null else b.optString("image")
                        )

                        val testimonialsJson = json.optJSONArray("testimonials")
                        val testimonials = mutableListOf<Testimonial>()
                        if (testimonialsJson != null) {
                            for (i in 0 until testimonialsJson.length()) {
                                val t = testimonialsJson.getJSONObject(i)
                                testimonials.add(
                                    Testimonial(
                                        name = t.optString("name"),
                                        role = t.optString("role"),
                                        quote = t.optString("quote"),
                                        rating = t.optInt("rating", 5),
                                        photo = if (t.isNull("photo")) null else t.optString("photo")
                                    )
                                )
                            }
                        }

                        callback(HomeData(banner, testimonials), null)
                    } catch (e: Exception) {
                        callback(null, e)
                    }
                }
            }
        })
    }
}
