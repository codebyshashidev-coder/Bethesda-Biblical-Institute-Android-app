package com.bethesda.institute.net

import com.bethesda.institute.model.NewsItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object NewsApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches published news items from api/news.php.
     * Callback is invoked on a background thread.
     */
    fun fetchNews(apiUrl: String, limit: Int = 10, callback: (List<NewsItem>?, Exception?) -> Unit) {
        val url = "$apiUrl?limit=$limit"
        val request = Request.Builder().url(url).get().build()

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
                        val itemsJson: JSONArray = json.optJSONArray("items") ?: JSONArray()
                        val items = mutableListOf<NewsItem>()
                        for (i in 0 until itemsJson.length()) {
                            val o = itemsJson.getJSONObject(i)
                            items.add(
                                NewsItem(
                                    id = o.optInt("id"),
                                    title = o.optString("title"),
                                    slug = o.optString("slug"),
                                    category = o.optString("category"),
                                    excerpt = o.optString("excerpt", null),
                                    image = if (o.isNull("image")) null else o.optString("image"),
                                    isFeatured = o.optBoolean("is_featured", false),
                                    createdAt = o.optString("created_at")
                                )
                            )
                        }
                        callback(items, null)
                    } catch (e: Exception) {
                        callback(null, e)
                    }
                }
            }
        })
    }
}
