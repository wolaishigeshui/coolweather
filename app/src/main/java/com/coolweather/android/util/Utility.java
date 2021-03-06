package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Forecast1;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.Weather1;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据,并保存到数据库中
     * 访问http://guolin.tech/api/china，服务器返回如下数据
     * [{"id":1,"name":"北京"},{"id":2,"name":"上海"},...]
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                //关于json的解释参考了如下链接
                // https://blog.csdn.net/u014260748/article/details/41521123

                //从字符串String获得JSONObject对象和JSONArray对象
                //JSONObject  jsonObject  = new JSONObject ( String  str);
                //JSONArray jsonArray = new JSONArray(String    str  ) ;
                //由于在此处服务器传回的是类似[{"id":1,"name":"北京"},{"id":2,"name":"上海"},...]的
                // 数组对象，所以需要将response转换为json数组对象
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();i++){
                    //从JSONArray中获得JSONObject对象
                    //大家可以把JSONArray当成一般的数组来对待，只是获取的数据内数据的方法不一样
                    //JSONObject   jsonObject  =  jsonArray.getJSONObject(i) ;
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    //创建一个Province对象实例，准备添加到数据库中
                    Province province=new Province();
                    //为该实例设置多种属性，完善信息
                    //获取JSON内的数据
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //最后调用save()方法完成数据的添加操作
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据,并保存到数据库中
     * @param response
     * @param provinceId  每个市都对应着一个上级省，对应着多个下级区
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for (int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据,并保存到数据库中
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for (int i=0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response){
        try{
            Log.d("Weather_response1", response);
            //在这里服务器返回的是{}类型，所以要使用jsonobject
            JSONObject jsonObject=new JSONObject(response);
            //获取json对象中属性名为"HeWeather"的json数组
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            Log.d("Weather_response11", String.valueOf(jsonArray));
            //获取json数组中的第一项并转型为字符串类型
            String weatherContent=jsonArray.getJSONObject(0).toString();
            Log.d("Weather_response111", weatherContent);
            //Gson().fromJson
            // 将weatherContent json数据转换为Weather类型的对象，并返回该对象
            //  参考链接： https://www.jianshu.com/p/bca8117ad49e
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Weather1 handleWeather1Response(String response){
        try{
            Log.d("Weather_response5", response);
            //在这里服务器返回的是{}类型，所以要使用jsonobject
            JSONObject jsonObject=new JSONObject(response);

            String weatherContent=jsonObject.toString();
            Log.d("Weather_response555", weatherContent);
            //Gson().fromJson
            // 将weatherContent json数据转换为Weather类型的对象，并返回该对象
            //  参考链接： https://www.jianshu.com/p/bca8117ad49e
            return new Gson().fromJson(weatherContent, Weather1.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<Forecast1> handleForecast1Response(String response){
        try{
            Log.d("Weather_response1", response);
            //在这里服务器返回的是{}类型，所以要使用jsonobject
            JSONObject jsonObject=new JSONObject(response);
            //获取json对象中属性名为"data"的json数组
            JSONArray jsonArray=jsonObject.getJSONArray("data");
            Log.d("Weather_response11", String.valueOf(jsonArray));
            List<Forecast1> forecast1List=new ArrayList<>();
            for (int i=0;i<jsonArray.length();i++){

                //获取json数组中的第i项并转型为字符串类型
                String weatherContent=jsonArray.getJSONObject(i).toString();
                Log.d("Weather_response111", weatherContent);
                //Gson().fromJson
                // 将weatherContent json数据转换为Weather类型的对象
                //  参考链接： https://www.jianshu.com/p/bca8117ad49e
                forecast1List.add(new Gson().fromJson(weatherContent,Forecast1.class));
            }
            return forecast1List;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }
}
