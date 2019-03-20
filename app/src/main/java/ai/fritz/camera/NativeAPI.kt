package ai.fritz.camera

import ai.fritz.camera.Product
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*

private val API_QUERY_URL: String = "https://3cuaap8e88.execute-api.us-east-2.amazonaws.com/production/suggestions?"

/**
 * @param [productInfo] JSON containing a single product's information
 *
 * @return Product object containing data from productInfo
 */
fun getProduct(productInfo: JSONObject): Product {
    val name: String = productInfo.getString("name")
    val price: Double = productInfo.getDouble("price")
    val imgUrl: String = productInfo.getJSONArray("images").getString(0)
    val debugTag = "getProduct"
    Log.d(debugTag, "product name: $name")
    Log.d(debugTag, "product price: $price")
    Log.d(debugTag, "img url: $imgUrl")
    return Product(name,price, imgUrl)
}

/**
 * @param [jsonString] json-like string provided as a response to an API call
 *
 * @return List of results as Product objects
 */
fun getProducts(jsonString: String): ArrayList<Product> {
    var results: ArrayList<Product> = ArrayList()

    val apiResponse = JSONArray(jsonString)
    for (i in 0 until apiResponse.length()) {
        results.add(getProduct(apiResponse.getJSONObject(i)))
    }

    return results
}

/**
 * @param [product] Product to query
 * @param [numOfProducts] Number of products to return
 * @param [minPrice] Minimum product price
 * @param [maxPrice] Maximum product price
 * @param [sort] Whether to sort by price or not
 * @param [imgSize] Size of product images (1-4)
 *
 * @return json-like string provided as a response from the API call
 */
fun getAPIResponse(product: String, numOfProducts: Int? = null, minPrice: Int? = null, maxPrice: Int? = null, sort: Boolean = false, imgSize: Int = 2): String {
    Log.d("APICALL", product)
    var queryUrl: String = API_QUERY_URL + "tag=${product.replace(' ', '+')}"
    Log.d("DEMOSPRINT3", "hitting endpoint: ${queryUrl}")

    return URL(queryUrl).readText()
}

/**
 * @param [product] Product to query
 * @param [numOfProducts] Number of products to return
 * @param [minPrice] Minimum product price
 * @param [maxPrice] Maximum product price
 * @param [sort] Whether to sort by price or not
 * @param [imgSize] Size of product images (1-4)
 *
 * @return List of results as Product objects
 */
fun getSuggestionsIkea(product: String, numOfProducts: Int? = null, minPrice: Int? = null, maxPrice: Int? = null, sort: Boolean = false, imgSize: Int = 2): ArrayList<Product> {
    Log.d("APICALL", product)
    val response = getAPIResponse(product, numOfProducts, minPrice, maxPrice, sort, imgSize)
    return getProducts(response)
}