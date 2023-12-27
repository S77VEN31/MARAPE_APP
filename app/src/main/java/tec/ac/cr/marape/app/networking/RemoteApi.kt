package tec.ac.cr.marape.app.networking

import com.google.gson.Gson
import tec.ac.cr.marape.app.model.LookupResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val TAG = "Z:RemoteApi"

object RemoteApi {
  // TODO: Get this thing out of here
  private val apiKey = "sbgsgicjqfdouka6azghkh9sey404y"

  private val baseUrl =
    "https://api.barcodelookup.com/v3/products?barcode=%s&key=%s"

  fun getProduct(
    barcode: String,
    callback: (LookupResponse) -> Unit,
    onError: (Exception) -> Unit
  ) {
    Thread(Runnable {
      var connection: HttpURLConnection? = null
      try {
        connection = URL(baseUrl.format(barcode, apiKey)).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.doInput = true

        val reader = InputStreamReader(connection.inputStream)
        reader.use { input ->
          val response = StringBuilder()
          val bufferedReader = BufferedReader(input)

          bufferedReader.forEachLine {
            response.append(it.trim())
          }
          val data = parseResponse(response.toString())
          callback(data)
        }
      } catch (e: Exception) {
        //TODO: do something with the exception
        onError(e)
      }
      connection?.disconnect()
    }).start()
  }

  private fun parseResponse(data: String): LookupResponse {
    return Gson().fromJson(data, LookupResponse::class.java)
  }
}