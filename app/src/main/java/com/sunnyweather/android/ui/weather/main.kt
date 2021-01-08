package com.sunnyweather.android.ui.weather

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.location.Geocoder
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.ui.place.PlaceAdapter
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*
import com.sunnyweather.android.ui.place.PlaceViewModel
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.gps.*
import kotlinx.android.synthetic.main.now.*

class main : AppCompatActivity() {
    private var tv: TextView? = null
    private var lm: LocationManager? = null
    private var criteria: Criteria? = null
    private var location: Location? = null

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gps)

        ok.setOnClickListener {
            val viewModel by lazy { ViewModelProviders.of(this).get(PlaceViewModel::class.java) }
            val place = viewModel.getSavedPlace()
            val intent = Intent(this, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
        }

        tv = findViewById<View>(R.id.tv) as TextView
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS已关闭,请手动开启GPS后再试！", Toast.LENGTH_SHORT).show()
            return
        } else {
            Toast.makeText(this, "GPS定位中...", Toast.LENGTH_SHORT).show()
        }
        criteria = Criteria()
        criteria!!.accuracy = Criteria.ACCURACY_FINE // 设置精确度
        criteria!!.isAltitudeRequired = true // 设置请求海拔
        criteria!!.isBearingRequired = true // 设置请求方位
        criteria!!.isCostAllowed = true // 设置允许运营商收费
        criteria!!.powerRequirement = Criteria.POWER_LOW // 低功耗
        val provider = lm!!.getBestProvider(criteria!!, true)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        location = lm!!.getLastKnownLocation(provider!!)
        newLocalGPS(location)
        // 监听1秒一次 忽略位置变化
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        lm!!.requestLocationUpdates(provider, 1 * 1000.toLong(), 0f, locationListener())
    }

    internal inner class locationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            // TODO Auto-generated method stub
            newLocalGPS(location)
        }

        override fun onProviderDisabled(provider: String) {
            // TODO Auto-generated method stub
            newLocalGPS(null)
        }

        override fun onProviderEnabled(provider: String) {
            // TODO Auto-generated method stub
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            // TODO Auto-generated method stub
        }
    }

    private fun newLocalGPS(location: Location?) {
        if (location != null) {
            val latitude = location.latitude //纬度
            val longitude = location.longitude // 经度

            val a = getAddress(latitude,longitude)
            tv!!.text = """
                纬度：$latitude
                经度：$longitude
                城市：$a
                """.trimIndent()

            val viewModel by lazy { ViewModelProviders.of(this).get(PlaceViewModel::class.java) }
            val place = viewModel.getSavedPlace()
            place.location.lat = "$latitude"
            place.location.lng = "$longitude"
            place.name = "$a"
            PlaceDao.savePlace(place)

        } else {
            // 未获取地理信息位置
            tv!!.text = "地理信息位置未知或正在获取地理信息位置中..."
        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(
                latitude,
                longitude,1
            )
            if (addresses.size > 0) {
                val address = addresses[0]
                val data = address.toString()
                val startPlace = data.indexOf("feature=") + "feature=".length
                val endplace = data.indexOf(",", startPlace)
                val place = data.substring(startPlace, endplace)
                return place
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "获取失败"
    }

    private fun exit() {
        val builder = AlertDialog.Builder(this@main)
        builder.setMessage("确认退出吗？")
        builder.setTitle("提示")
        builder.setPositiveButton("确认") { arg0, arg1 -> // TODO Auto-generated method stub
            arg0.dismiss()
            val actMgr =
                getSystemService(ACTIVITY_SERVICE) as ActivityManager
            actMgr.restartPackage(packageName)
        }
        builder.setNegativeButton(
            "取消"
        ) { dialog, which -> // TODO Auto-generated method stub
            dialog.dismiss()
        }
        builder.create().show()
    }
}