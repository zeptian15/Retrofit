package com.fakhrimf.retrofit.show

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fakhrimf.retrofit.BuildConfig
import com.fakhrimf.retrofit.R
import com.fakhrimf.retrofit.model.ShowModel
import com.fakhrimf.retrofit.model.ShowResponse
import com.fakhrimf.retrofit.utils.source.remote.ApiClient
import com.fakhrimf.retrofit.utils.source.remote.ApiInterface
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ShowVM(application: Application): AndroidViewModel(application) {
    lateinit var showList: ArrayList<ShowModel>
    private val context = getApplication() as Context
    private var isLoaded = false

    fun setRecycler(recyclerView: RecyclerView, listener: ShowUserActionListener, type: String, srl: SwipeRefreshLayout) {
        if (!isLoaded) {
            srl.isRefreshing = true
            recyclerView.apply {
                animate()
                    .alpha(TRANSPARENT_ALPHA)
                    .setDuration(DURATION)
                    .setListener(null)
            }
            GlobalScope.launch(Dispatchers.IO) {
                //Background Thread
                val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
                getPopularShow(apiInterface, recyclerView, listener, type)
                getLatestShow(apiInterface)
                delay(2000)

                //Main Thread
                withContext(Dispatchers.Main) {
                    if (type == VALUE_LIST || type == VALUE_CARD) recyclerView.layoutManager =
                        LinearLayoutManager(getApplication())
                    else recyclerView.layoutManager = GridLayoutManager(getApplication(), 2)
                    recyclerView.apply {
                        animate()
                            .alpha(OPAQUE_ALPHA)
                            .setDuration(DURATION)
                            .setListener(null)
                    }
                    srl.isRefreshing = false
                }
            }
        } else if (isLoaded) {
            if (type == VALUE_LIST || type == VALUE_CARD) recyclerView.layoutManager =
                LinearLayoutManager(getApplication())
            else recyclerView.layoutManager = GridLayoutManager(getApplication(), 2)
            when (type) {
                VALUE_LIST -> recyclerView.adapter =
                    ShowListAdapter(showList, listener)
                VALUE_CARD -> recyclerView.adapter =
                    ShowCardAdapter(showList, listener)
                else -> recyclerView.adapter = ShowGridAdapter(showList, listener)
            }
        }
    }

    fun onRefresh(recyclerView: RecyclerView, listener: ShowUserActionListener, type: String, srl: SwipeRefreshLayout) {
        srl.isRefreshing = true
        recyclerView.apply {
            animate()
                .alpha(TRANSPARENT_ALPHA)
                .setDuration(DURATION)
                .setListener(null)
        }
        GlobalScope.launch (Dispatchers.IO) {
            //Background Thread
            val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
            getPopularShow(apiInterface, recyclerView, listener, type)
            getLatestShow(apiInterface)
            delay(2000)

            //Main Thread
            withContext(Dispatchers.Main){
                if (type == VALUE_LIST || type == VALUE_CARD) recyclerView.layoutManager =
                    LinearLayoutManager(context)
                else recyclerView.layoutManager = GridLayoutManager(context, 2)
                recyclerView.apply {
                    animate()
                        .alpha(OPAQUE_ALPHA)
                        .setDuration(DURATION)
                        .setListener(null)
                }
                srl.isRefreshing = false
            }
        }
    }

    private fun getPopularShow(apiInterface: ApiInterface, recyclerView: RecyclerView, listener: ShowUserActionListener, type: String) {
        val apiKey = BuildConfig.API_KEY
        val currentLocale = context.resources.configuration.locales.get(0)
        var locale = currentLocale.toString().split("_")[1].toLowerCase(Locale.ENGLISH)
        if (locale != LOCALE_ID) {
            locale = LOCALE_EN
        }
        val call: Call<ShowResponse> = apiInterface.getPopularShow(apiKey, locale)
        call.enqueue(object : Callback<ShowResponse> {
            override fun onFailure(call: Call<ShowResponse>, t: Throwable) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error),
                    Toast.LENGTH_LONG
                ).show()
                Log.d(TAG_ERROR, SHOW_POPULAR_FAIL)
            }

            override fun onResponse(call: Call<ShowResponse>, response: Response<ShowResponse>) {
                showList = response.body()!!.results

                for (i in 0 until showList.size) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val date: Date = sdf.parse(showList[i].releaseDate)
                    val check = Date() > date
                    if (check) showList[i].releaseDate =
                        context.getString(R.string.first_aired) + " " + showList[i].releaseDate
                    else showList[i].releaseDate = context.getString(R.string.unreleased)
                    if (showList[i].overview == "") showList[i].overview =
                        context.getString(R.string.unavailable)
                    if (showList[i].vote == "0.0" || showList[i].vote == "0") showList[i].vote =
                        context.getString(R.string.unrated)
                    else showList[i].vote =
                        context.getString(R.string.rating) + " " + showList[i].vote + " / 10"
                }

                when (type) {
                    VALUE_CARD -> recyclerView.adapter =
                        ShowCardAdapter(showList, listener)
                    VALUE_LIST -> recyclerView.adapter =
                        ShowListAdapter(showList, listener)
                    else -> recyclerView.adapter = ShowGridAdapter(showList, listener)
                }
                isLoaded = true
            }
        })
    }

    private fun getLatestShow(apiInterface: ApiInterface): ShowModel? {
        val apiKey = BuildConfig.API_KEY
        val showModel: ShowModel? = null
        val call: Call<ShowModel> = apiInterface.getLatestShow(apiKey, "en")
        call.enqueue(object : Callback<ShowModel> {
            override fun onFailure(call: Call<ShowModel>, t: Throwable) {
                Log.d(TAG_ERROR, SHOW_LATEST_FAIL)
            }

            override fun onResponse(call: Call<ShowModel>, response: Response<ShowModel>) {
//                val title = response.body()!!.title
//                val path = response.body()!!.posterPath
//                val overview = response.body()!!.overview
            }

        })
        return showModel
    }

    companion object {
        private const val LOCALE_ID = "id"
        private const val LOCALE_EN = "en"
        private const val SHOW_LATEST_FAIL = "Failed to fetch Latest Shows"
        private const val SHOW_POPULAR_FAIL = "Failed to fetch Popular Shows"
        private const val TAG_ERROR = "ERR"
        private const val VALUE_CARD = "card"
        private const val VALUE_LIST = "list"
        private const val DURATION: Long = 250
        private const val TRANSPARENT_ALPHA = 0.0F
        private const val OPAQUE_ALPHA = 1.0F
    }
}