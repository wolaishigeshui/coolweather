package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 使用gson完成对服务器返回的json数据进行解析需要创建类来对应服务器返回的数据，服务器返回的数据见本文件最下方
 * 使用@SerializedName("daily_forecast")
 *     public List<Forecast> forecastList;
 *     可以将服务器传来的daily_forecast属性与类实例的forecastList相对应，这么做的原因是因为，有可
 *     能服务器传来的属性名无法在java代码中作为变量名，所以采取这种方式让类实例中的变量和数据流
 *     中的对应数据建立映射。
 *
 * 没有必要将服务器返回的所以属性全部建立映射，只需要建立需要的就行了。
 */
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}


/*
{
	"HeWeather": [{
		"basic": {
			"cid": "CN101270103",
			"location": "新都",
			"parent_city": "��都",
			"admin_area": "四川",
			"cnty": "中国",
			"lat": "40.81830978",
			"lon": "111.67079926",
			"tz": "+8.00",
			"city": "新都",
			"id": "CN101270103",
			"update": {
				"loc": "2020-02-12 21:09",
				"utc": "2020-02-12 13:09"
			}
		},
		"update": {
			"loc": "2020-02-12 21:09",
			"utc": "2020-02-12 13:09"
		},
		"status": "ok",
		"now": {
			"cloud": "10",
			"cond_code": "101",
			"cond_txt": "多云",
			"fl": "-10",
			"hum": "54",
			"pcpn": "0.0",
			"pres": "1016",
			"tmp": "0",
			"vis": "16",
			"wind_deg": "345",
			"wind_dir": "西北风",
			"wind_sc": "6",
			"wind_spd": "43",
			"cond": {
				"code": "101",
				"txt": "多云"
			}
		},
		"daily_forecast": [{
			"date": "2020-02-13",
			"cond": {
				"txt_d": "多云"
			},
			"tmp": {
				"max": "5",
				"min": "-8"
			}
		}, {
			"date": "2020-02-14",
			"cond": {
				"txt_d": "晴"
			},
			"tmp": {
				"max": "4",
				"min": "-8"
			}
		}, {
			"date": "2020-02-15",
			"cond": {
				"txt_d": "晴"
			},
			"tmp": {
				"max": "9",
				"min": "-5"
			}
		}, {
			"date": "2020-02-16",
			"cond": {
				"txt_d": "多云"
			},
			"tmp": {
				"max": "5",
				"min": "-8"
			}
		}, {
			"date": "2020-02-17",
			"cond": {
				"txt_d": "阴"
			},
			"tmp": {
				"max": "4",
				"min": "-8"
			}
		}, {
			"date": "2020-02-18",
			"cond": {
				"txt_d": "多云"
			},
			"tmp": {
				"max": "9",
				"min": "-5"
			}
		}],
		"aqi": {
			"city": {
				"aqi": "56",
				"pm25": "7",
				"qlty": "良"
			}
		},
		"suggestion": {
			"comf": {
				"type": "comf",
				"brf": "较不舒适",
				"txt": "白天天气较凉，且风力较强，您会感觉偏冷，不很舒适，请注意添加衣物，以防感冒。"
			},
			"sport": {
				"type": "sport",
				"brf": "较适宜",
				"txt": "天气较好，但考虑风力较强且气温较低，推荐您进行室内运动，若在户外运动注意防风并适当增减衣物。"
			},
			"cw": {
				"type": "cw",
				"brf": "较不宜",
				"txt": "较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。"
			}
		},
		"msg": "所有天气数据均为模拟数据，仅用作学习目的使用，请勿当作真实的天气预报软件来使用。"
	}]
}


 */