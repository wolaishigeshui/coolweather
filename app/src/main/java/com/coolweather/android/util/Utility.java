package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
}
