package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    /**
     *发起一条HTTP请求只需要调用此方法，传入请求地址，并注册一个回调来处理服务器响应就可以了
     * @param address
     * @param callback OkHttp库中自带的一个回调接口
     */
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //创建一个OKHttpClient实例
        OkHttpClient client=new OkHttpClient();

        //想要发起一条HTTP请求，就需要创建一个Request对象
        //Request request=new Request.Builder().build();  本行代码创建了一个空的Request对象，没有什
        // 么实际作用，我们可以在最终的build()方法之前连缀很多其他方法来丰富这个Request对象
        Request request=new Request.Builder()
                .url(address)//通过url()方法设置目标的网络地址
                .build()
                ;

        //Response response=client.newCall(request).execute();本行调用newCall()方法创建一个Call
        // 对象，并调用他的execute()方法来发送同步请求并获取服务器返回的数据
        //而enqueue()方法为异步请求
        //OkHttp在enqueue()方法的内部已经帮我们开好子线程了，他会在子线程中去执行HTTP请求，并将最终
        // 的请求结果回调到okhttp3.Callback当中
        client.newCall(request).enqueue(callback);
    }
}
