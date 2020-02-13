package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    //最外层为ScrollView，使得手机屏幕可以上下拖动
    private ScrollView weatherLayout;

    //顶栏设置区域名称
    private TextView titleCity;

    //顶栏设置预报更新时间
    private TextView titleUpdateTime;

    //设置温度
    private TextView degreeText;

    //设置天气简略描述，例如 多云，晴，etc.
    private TextView weatherInfoText;

    //设置未来连续几天的天气预报，稍后会在该线性布局中动态添加子布局
    private LinearLayout forecastLayout;

    //设置aqi指数
    private TextView aqiText;

    //设置pm25指数
    private TextView pm25Text;

    //设置生活建议——舒适度
    private TextView comfortText;

    //设置生活建议——洗车指数
    private TextView carWashText;

    //设置生活建议——运动建议
    private TextView sportText;

    //设置背景图片
    private ImageView bingPicImg;

    //刷新逻辑
    public SwipeRefreshLayout swipeRefresh;

    //
    public DrawerLayout drawerLayout;

    //
    private Button navButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);
        swipeRefresh=findViewById(R.id.swipe_refresh);
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        //设置下拉刷新进度条的颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//@Deprecated     PreferenceManager
        //SharedPreferences对象中提供了一系列的get方法
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        //定义weatherId变量，用于记录城市的天气ID
        final String weatherId;
        if (weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        //设置一个下拉刷新的监听器，当触发了下拉刷新操作的时候，就会回调这个监听器的onRefresh()方法，
        //我们在这里去调用 requestWeather(weatherId)方法请求天气信息就可以了
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开滑动菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });



//        //从Intent实例中获取weather_id
//        String weatherId=getIntent().getStringExtra("weather_id");
//        //设置最外层，也就是全部布局不可见
//        weatherLayout.setVisibility(View.INVISIBLE);
//        //利用weatherId向服务器请求指定城市天气数据
//        requestWeather(weatherId);
    }
//http://guolin.tech/api/weather?cityid=CN101190401&key=7205afdb284a46e4b6f139642a483940
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=7205afdb284a46e4b6f139642a483940";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"没有成功接收到服务器的数据，获取天气信息失败",Toast.LENGTH_SHORT).show();
                        //当请求结束后，还需要调用该方法并传入false，用于表示刷新时间结束，并隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText=response.body().string();
                //获取解析后生成的weather对象
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果成功获取到天气对象，并且获取到的天气对象不是空的，
                        // 那么就将获取到的数据存入缓存中，并更新UI
                        if (weather!=null&&"ok".equals(weather.status)){
                            //想要使用SharedPreferences来存储数据，首先需要获取到SharedPreferences对象
                            //下面通过PreferenceManager.getDefaultSharedPreferences()方法来获取该对象
                            //再通过调用该对象的edit()方法来获取一个SharedPreferences.Editor对象
                            //接下来才是向SharedPreferences.Editor对象中添加数据，比如添加一个布尔型数据，
                            // 就使用putBoolean()方法，添加一个字符串则使用putString()方法，以此类推
                            //最后调用apply()方法将添加的数据提交，也就是保存到收集文件中，从而完成数据存储操作
                            //但是PreferenceManager对象已经过时，需要采取替代方案

                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
//                            Intent intent=new Intent(this, AutoUpdateService.class);
//                            startService(intent);
                        }else {
                            Toast.makeText(WeatherActivity.this,"成功接收到服务器的数据，但是获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    private void showWeatherInfo(Weather weather){
        if (weather!=null&&"ok".equals(weather.status)){
            String cityName=weather.basic.cityName;
            String updateTime=weather.basic.update.updateTime.split(" ")[1];
            String degree=weather.now.temperature+"℃";
            String weatherInfo=weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for (Forecast forecast:weather.forecastList){
                //inflate()方法    加载layout文件来转换成View
                //public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot)
                //如果root为null或者attachToRoot为false时，则调用layout.xml中的根布局的属性并且将其作为一个View对象返回。
                //如果root不为null，但attachToRoot为false时，则先将layout.xml中的根布局转换为一个View对象，再调用传进来的root的布局属性设置给这个View，然后将它返回。
                //如果root不为null，且attachToRoot为true时，则先将layout.xml中的根布局转换为一个View对象，再将它add给root，最终再把root返回出去。（两个参数的inflate如果root不为null也是相当于这种情况）
                //此处应该是第二种情况
                //参考此链接：https://www.jianshu.com/p/3f871d95489c
                View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText=view.findViewById(R.id.date_text);
                TextView infoText=view.findViewById(R.id.info_text);
                TextView maxText=view.findViewById(R.id.max_text);
                TextView minText=view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                minText.setText(forecast.temperature.min);
                forecastLayout.addView(view);
            }
            if (weather.aqi!=null){
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort="舒适度："+weather.suggestion.comfort.info;
            String carWash="洗车指数："+weather.suggestion.carWash.info;
            String sport="运动建议："+weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);
            //??????为什么这句话放在这里就不报错，放在上面就要报错
            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(WeatherActivity.this,"待显示的天气对象为空，获取天气信息失败",Toast.LENGTH_SHORT).show();
        }

    }
}
