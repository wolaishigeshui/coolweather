package com.coolweather.android;

import android.app.ProgressDialog;
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
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
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

    /**
     * 选中的城市
     */
    private City selectedCity;

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
        //此处应该是第二种情况，
        //参考此链接：https://www.jianshu.com/p/3f871d95489c
        View view=inflater.inflate(R.layout.choose_area,container,false);
        Log.d("inflate_test", String.valueOf(container));//日志打印出来的全是空指针？？为什么？
        // 2020-02-12 17:05:05.824 10958-10958/com.coolweather.android D/inflate_test: null
        //2020-02-12 17:05:53.544 10958-10958/com.coolweather.android D/inflate_test: null
        //而且不是太明白触发机制，不能复现这个情况
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
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
        provinceList=LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
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
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= LitePal
                .where("provinceid = ?"
                        //查找provinceid为当前selectedProvince的ID的数据
                        ,String.valueOf(selectedProvince.getId()))
                //find返回值为List<T>
                // 来自官方的示例 https://github.com/LitePalFramework/LitePal
                //List<Song> songs = LitePal.where("name like ? and duration < ?", "song%", "200").order("duration").find(Song.class);
                .find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
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
        titleText.setText(selectedCity.getCityName());
        titleText.setVisibility(View.VISIBLE);
        countyList=LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
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
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
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
