package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.City1;
import com.coolweather.android.db.County;
import com.coolweather.android.db.County1;
import com.coolweather.android.db.Province;
import com.coolweather.android.db.Province1;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.GetJsonDataUtil;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 碎片
 * 1 定义碎片的视图类，继承于Fragment，需要重写几个方法
 *      onCreateView方法
 *      onActivityCreated方法
 *
 * 2 需要给出该碎片视图类对应的xml布局文件，在本项目中是choose_area.xml布局文件
 *
 * 3 想要显示这个碎片，就需要用<fragment></fragment>标签，通过标签内的android:name属性
 * 来显式指明要添加的碎片类名，一定要将类的包名也加上，在本项目中可以在activity_main.xml布局
 * 文件中找到该标签以及name属性
 */
public class ChooseAreaFragment extends Fragment {

    private List<Province1> provinceList1=new ArrayList<>();
    private List<City1>cityList1=new ArrayList<>();
    private List<County1>countyList1=new ArrayList<>();

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;//@Deprecated
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();






    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City>cityList;

    /**
     * 县列表
     */
    private List<County>countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;
    private Province1 selectedProvince1;

    /**
     * 选中的城市
     */
    private City selectedCity;
    private City1 selectedCity1;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     *
     * @param inflater 它的作用类似于findViewById()。不同点是LayoutInflater是用来找res/layout/下
     *                 的xml布局文件，并且实例化；而findViewById()是找xml布局文件下的
     *                 具体widget控件(如Button、TextView等)。
     * @param container 是容器,View放在里面.
     * @param savedInstanceState 保存当前状态,在activity的生命周期中，只要离开了可见阶段，或者说
     *                           失去了焦点，activity就很可能被进程终止了！，
     *                           被KILL掉了，，这时候，就需要有种机制，能
     *                           保存当时的状态，这就是savedInstanceState的作用
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflate()方法    加载layout文件来转换成View
        //public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot)
        //如果root为null或者attachToRoot为false时，则调用layout.xml中的根布局的属性并且将其作为一个View对象返回。
        //如果root不为null，但attachToRoot为false时，则先将layout.xml中的根布局转换为一个View对象，再调用传进来的root的布局属性设置给这个View，然后将它返回。
        //如果root不为null，且attachToRoot为true时，则先将layout.xml中的根布局转换为一个View对象，再将它add给root，最终再把root返回出去。（两个参数的inflate如果root不为null也是相当于这种情况）
        //此处应该是第二种情况，但是通过logd日志，发现container似乎一直为null，也就是说，在此处应该是第一种情况
        //参考此链接：https://www.jianshu.com/p/3f871d95489c
        View view=inflater.inflate(R.layout.choose_area,container,false);
        Log.d("inflate_test", String.valueOf(container));//日志打印出来的全是空指针？？为什么？
        // 2020-02-12 17:05:05.824 10958-10958/com.coolweather.android D/inflate_test: null
        //2020-02-12 17:05:53.544 10958-10958/com.coolweather.android D/inflate_test: null
        //而且不是太明白触发机制，不能复现这个情况,好像是隐藏该页面之后就会再次调用onCreateView方法
        titleText=view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        //初始化适配器
        //ArrayAdapter有多个构造函数，参考链接如下
        //https://blog.csdn.net/guyuealian/article/details/46790873
        //此处调用了下面的这个构造方法，分析如下构造方法
        // public ArrayAdapter(@NonNull Context context, @LayoutRes int resource,
        //            @NonNull List<T> objects)
        //第一个参数 内容上下文？？？？不懂
        //待参考的
        //链接如下
        // https://blog.csdn.net/yx19861211/article/details/93858814
        //第二个参数
        // 需要传入列表单项的布局文件，可以自定义，库中也有预设，如下
        //simple_list_item_1 : 单独一行的文本框
        //simple_list_item_2 : 两个文本框组成
        //simple_list_item_checked : 每项都是由一个已选中的列表项
        //simple_list_item_multiple_choice : 都带有一个复选框
        //simple_list_item_single_choice : 都带有一个单选钮
        //参考如下链接
        // https://blog.csdn.net/GL_X85/article/details/77943601
        //第三个参数 想要表示的数据
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        //绑定适配器
        listView.setAdapter(adapter);


//        String s=new GetJsonDataUtil().getJson(getActivity(),"city.json");
//        Log.d("abcd", "onActivityCreated: "+provinceList1);
//        if (provinceList1.size()<=0){
//            try {
//                JSONArray jsonArray1=new JSONArray(s);
//                for (int i=0;i<jsonArray1.length();i++){
//                    JSONObject jsonObject1=jsonArray1.getJSONObject(i);
//                    String temp=jsonObject1.getString("provinceEn");
//                    int j=0;
//                    for (;j<provinceList1.size();j++){
//                        if (provinceList1.get(j).getProvinceEn().equals(temp))break;
//                    }
//                    if (j==provinceList1.size()){
//                        Province1 province1=new Province1();
//                        province1.setProvinceEn(temp);
//                        province1.setProvinceZh(jsonObject1.getString("provinceZn"));
//                        provinceList1.add(province1);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        for (int k=0;k<provinceList1.size();k++){
//            Log.d("provinceList1"+k, k+"    "+provinceList1.get(k));
//        }
//
//        Log.d("getJson", s);
//        Log.d("getJson1", String.valueOf(s.length()));




        return view;
    }





    /**
     * onCreateView()：每次创建、绘制该Fragment的View组件时回调该方法，Fragment将会显示该方法返回的View组件。
     * onActivityCreated()：当Fragment所在的Activity被启动完成后回调该方法。
     * 参考链接如下：
     * https://blog.csdn.net/agent_bin/article/details/52050307
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        String s=new GetJsonDataUtil().getJson(getActivity(),"city.json");
        Log.d("abcd", "onActivityCreated: "+provinceList1);
        if (provinceList1.size()<=0){
            try {
                JSONArray jsonArray1=new JSONArray(s);
                for (int i=0;i<jsonArray1.length();i++){
                    JSONObject jsonObject1=jsonArray1.getJSONObject(i);
                    String id=jsonObject1.getString("id");
                    String cityEn=jsonObject1.getString("cityEn");
                    String cityZh=Utility.decode(jsonObject1.getString("cityZh"));
                    String leaderEn=jsonObject1.getString("leaderEn");
                    String leaderZh=Utility.decode(jsonObject1.getString("leaderZh"));
                    String lat=jsonObject1.getString("lat");
                    String lon=jsonObject1.getString("lon");
                    String provinceEn=jsonObject1.getString("provinceEn");
                    String provinceZh=Utility.decode(jsonObject1.getString("provinceZh"));



                    if (cityEn.equals(leaderEn)){//说明这是一个市
                        if (cityEn.equals("danleng")){
                            Log.d("QWER", "++++++++++++++++++++++++++++++"+cityZh);
                        }
                        City1 city1=new City1();
                        city1.setId(id);
                        city1.setCityEn(cityEn);
                        city1.setCityZh(cityZh);
                        city1.setLeaderEn(leaderEn);
                        city1.setLeaderZh(leaderZh);
                        city1.setLat(lat);
                        city1.setLon(lon);
                        city1.setProvinceEn(provinceEn);
                        city1.setProvinceZh(provinceZh);
                        cityList1.add(city1);
                    }else {
                        if (cityEn.equals("danleng")){
                            Log.d("QWER", "---------------------"+cityZh);
                        }
                        County1 county1=new County1();
                        county1.setId(id);
                        county1.setCityEn(cityEn);
                        county1.setCityZh(cityZh);
                        county1.setLeaderEn(leaderEn);
                        county1.setLeaderZh(leaderZh);
                        county1.setLat(lat);
                        county1.setLon(lon);
                        county1.setProvinceEn(provinceEn);
                        county1.setProvinceZh(provinceZh);
                        countyList1.add(county1);
                    }






                    int j=0;
                    for (;j<provinceList1.size();j++){
                        if (provinceList1.get(j).getProvinceEn().equals(provinceEn)||provinceList1.get(j).getProvinceZh().equals(provinceZh))break;
                    }
                    if (j==provinceList1.size()){
                        Province1 province1=new Province1();
                        province1.setProvinceEn(provinceEn);
                        Log.d("provinceList1", ":::::1:::::"+provinceEn);
                        province1.setProvinceZh(provinceZh);
//                        province1.setProvinceZh("中国");
                        Log.d("provinceList1", "::::::2::::"+provinceZh);
                        Log.d("provinceList1", "::::::3::::"+province1.getProvinceZh());
                        provinceList1.add(province1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("provinceList1", "asdasdasdasd");
        for (int k=0;k<provinceList1.size();k++){
            Log.d("provinceList1", k+"    "+provinceList1.get(k));
        }
        for (int k=0;k<cityList1.size();k++){
            Log.d("cityList1", k+"    "+cityList1.get(k));
        }
        for (int k=0;k<countyList1.size();k++){
            Log.d("countyList1", k+"    "+countyList1.get(k));
            if (countyList1.get(k).getCityEn().equals("danleng")){
                Log.d("QWER", "<><><><><><><><><><><><><><><>"+countyList1.get(k).getCityZh());
            }
        }
        for (int k=0;k<cityList1.size();k++){
            String tempName=cityList1.get(k).getCityEn();
            int j;
            for (j=0;j<countyList1.size();j++){
                if (tempName.equals(countyList1.get(j).getLeaderEn()))break;
            }
            if (j==countyList1.size()){
                County1 county1=new County1();
                county1.setId(cityList1.get(k).getId());
                county1.setCityEn(cityList1.get(k).getCityEn());
                county1.setCityZh(Utility.decode(cityList1.get(k).getCityZh()));
                county1.setLeaderEn(cityList1.get(k).getLeaderEn());
                county1.setLeaderZh(Utility.decode(cityList1.get(k).getLeaderZh()));
                county1.setLat(cityList1.get(k).getLat());
                county1.setLon(cityList1.get(k).getLon());
                county1.setProvinceEn(cityList1.get(k).getProvinceEn());
                county1.setProvinceZh(Utility.decode(cityList1.get(k).getProvinceZh()));
                countyList1.add(county1);
            }
        }


        Log.d("getJson", s);
        Log.d("getJson1", String.valueOf(s.length()));






        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
//                    selectedProvince=provinceList.get(position);
                    selectedProvince1=provinceList1.get(position);
                    Toast.makeText(getContext(),selectedProvince1.getProvinceZh()+position,Toast.LENGTH_SHORT).show();
                    Log.d("aaa1p", selectedProvince1.toString()+position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
//                    selectedCity=cityList.get(position);
//                    selectedCity1=cityList1.get(position);
                    String tempCityZh=dataList.get(position);
                    Log.d("aaaa", "-+-+-+-+-+-+-+-+-+"+tempCityZh);
                    for (int i=0;i<cityList1.size();i++){
                        Log.d("aaaa", tempCityZh+"|||||||||"+cityList1.get(i).getCityZh());
                        if (tempCityZh.split(" ")[0].equals(cityList1.get(i).getCityZh())){
//                        if (tempCityZh==cityList1.get(i).getCityZh()){
                            selectedCity1=cityList1.get(i);
                            Log.d("aaaa", "------------++++++++++++: "+String.valueOf(selectedCity1));
                            break;
                        }
                    }
//                    Toast.makeText(getContext(),selectedCity1.getCityZh()+position,Toast.LENGTH_SHORT).show();
                    Log.d("aaa1f", String.valueOf(selectedCity1)+position);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){
//                    //如果进入了第三极界面，并且用户点击了某
//                    // 一项，则会触发该项的点击事件，进入此方法
//                    //通过position参数可以知道用户点击的具体位置，根据这个参数拿到对应第三极城市的天气id，
//                    // 以供之后的再次向服务器进行天气信息的请求。
//                    String weatherId=countyList.get(position).getWeatherId();
//                    //创建Intent实例，跳转到WeatherActivity活动，并向该实例中添加weather_id数据，以供
//                    // WeatherActivity活动获取到weather_id，从而向服务器请求天气数据
//                    Intent intent=new Intent(getActivity(), WeatherActivity.class);
//                    intent.putExtra("weather_id",weatherId);
//                    startActivity(intent);
//                     //销毁该活动
//                    getActivity().finish();
//                    String weatherId=countyList.get(position).getWeatherId();
//                    String weatherId=countyList1.get(position).getId();
                    String weatherId="";
                    String tempCountyEn=dataList.get(position);
                    for (int i=0;i<countyList1.size();i++){
                        if (tempCountyEn.split(" ")[2].equals(countyList1.get(i).getCityEn())){
                            weatherId=countyList1.get(i).getId();
                            Log.d("bbb1", String.valueOf(countyList1.get(i)));
                            break;
                        }
                    }


                    Log.d("weatherId", weatherId);
                    if (getActivity() instanceof MainActivity){
                        Log.d("where", "MainActivity");
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        Log.d("where", "WeatherActivity");
                        final WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                        //不要问我为什么要在这里加一个延时，因为第三方服务器拒绝我短时间内发送两个请求，笑哭emmmmmmm
                        final String finalWeatherId = weatherId;
                        TimerTask task=new TimerTask() {
                            @Override
                            public void run() {
                                activity.requestForecast1(finalWeatherId);
                            }
                        };
                        Timer timer=new Timer();
                        timer.schedule(task,1000);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有，再到服务器上查询
     * 将得到的数据设置到ListView中显示
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
//        provinceList=LitePal.findAll(Province.class);
//        if (provinceList.size()>0){
        if (provinceList1.size()>0){
            dataList.clear();
//            for (Province province:provinceList){
//                dataList.add(province.getProvinceName());
//            }
            for (Province1 province1:provinceList1){
                dataList.add(province1.getProvinceZh()+" 1 "+province1.getProvinceEn());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有，再到服务器上查询
     * 将得到的数据设置到ListView中显示
     */
    private void queryCities(){
//        titleText.setText(selectedProvince.getProvinceName());
        titleText.setText(selectedProvince1.getProvinceZh());
        backButton.setVisibility(View.VISIBLE);
//        cityList= LitePal
//                .where("provinceid = ?"
//                        //查找provinceid为当前selectedProvince的ID的数据
//                        ,String.valueOf(selectedProvince.getId()))
//                //find返回值为List<T>
//                // 来自官方的示例 https://github.com/LitePalFramework/LitePal
//                //List<Song> songs = LitePal.where("name like ? and duration < ?", "song%", "200").order("duration").find(Song.class);
//                .find(City.class);
//        if (cityList.size()>0){
        if (cityList1.size()>0){
            dataList.clear();
//            for (City city:cityList){
//                dataList.add(city.getCityName());
//            }
            for (City1 city1:cityList1){
                if (city1.getProvinceEn().equals(selectedProvince1.getProvinceEn()))
                    dataList.add(city1.getCityZh()+" 2 "+city1.getCityEn());
            }
            //notifyDataSetInvalidated()和notifyDataSetChanged()
            //当改变Adapter数据后，调用两个方法都会刷新视图
            //如何刷新？？？不懂
            //https://blog.csdn.net/pompip/article/details/51098766
            //https://blog.csdn.net/gongsunjinqian/article/details/27546145
            //https://blog.csdn.net/liang_duo_yu/article/details/81132547
            //https://blog.csdn.net/zd_1471278687/article/details/11527613
            //https://blog.csdn.net/lfdfhl/article/details/38851361
            adapter.notifyDataSetChanged();
            //public void setSelection(int position)
            //这个方法的作用就是将第position个item显示在listView的最上面一项
            //参考链接 https://blog.csdn.net/szyangzhen/article/details/47972509
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有，再到服务器上查询
     * 将得到的数据设置到ListView中显示
     */
    private void queryCounties(){
//        titleText.setText(selectedCity.getCityName());
        titleText.setText(selectedCity1.getCityZh()+selectedCity1.getCityEn());
        titleText.setVisibility(View.VISIBLE);
//        countyList=LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
//        if (countyList.size()>0){
        if (countyList1.size()>0){
            dataList.clear();
            for (County1 county1:countyList1){
                Log.d("bbbb", county1.getCityZh()+"|||||||||"+county1.getLeaderEn()+county1.getLeaderZh()+"|||||||||"+selectedCity1.getLeaderEn()+selectedCity1.getLeaderZh());
                if (county1.getLeaderEn().equals(selectedCity1.getLeaderEn())){
                    dataList.add(county1.getCityZh()+" 3 "+county1.getCityEn());
                    Log.d("bbbbb", county1.getCityEn()+"|||-------|||||"+selectedCity1.getCityEn());
                }



            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog();
//        String testAddress= "https://search.heweather.net/find?location=%E5%8C%97&key=7205afdb284a46e4b6f139642a483940";
//        String testAddress= "https://free-api.heweather.net/s6/weather/now?location=beijing&key=7205afdb284a46e4b6f139642a483940";
        //根据address和type向服务器请求省或市或县的数据
        HttpUtil.sendOkHttpRequest(address, new Callback() {//此CallBack回调在子线程中进行
//        HttpUtil.sendOkHttpRequest(testAddress, new Callback() {//此CallBack回调在子线程中进行
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败--从服务器上查询"+type,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //服务器顺利传回数据之后，会回调到这个方法，否则会回调到onFailure()方法
                //下面这行代码将从服务器传回的数据进行简单的解析，去掉头部，保留中间的body部分，并转为String类型
                String responseText=response.body().string();
                Log.d("response_body", String.valueOf(response.body()));
                Log.d("response_body_string", responseText);
                boolean result=false;
                if ("province".equals(type)){
                    //对服务器返回数据进行深度解析，并将解析到的结果储存到数据库中
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    //对服务器返回数据进行深度解析，并将解析到的结果储存到数据库中
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    //对服务器返回数据进行深度解析，并将解析到的结果储存到数据库中
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){//如果顺利接收到服务器传来的数据并顺利解析，将会进行下面的操作，
                    // 也就是重新调用查询方法，因为在上面的操作中已经将需要显示的数据从服务器获取，
                    // 并且储存到本地服务器中了，因此重新调用查询方法，就不会再次访问服务器，另外，
                    // 由于在查询方法中同时完成了视图的刷新操作，而UI操作只能放在主线程中，
                    // 所以这里需要将查询操作放在runOnUiThread()函数中
                    //此函数能够实现从子线程切换到主线程
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
