package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

/**
 * 本项目使用LitePal完成数据库的设计
 *
 * 1 数据类的创建需要继承自LitePalSupport
 *
 * 2 并且需要在AndroidManifest.xml文件特定位置（为application标签添加此属性）添加android:name="org.litepal.LitePalApplication"项
 *
 * 3 还需要在特定位置（app/src/main目录下，新建assets文件夹，再在assets目录下新建litepal.xml文件）创建litepal.xml文件
 * <litepal>
 *     <dbname value="cool_weather"/>
 *     <version value="1"/>
 *     <list>
 *         <mapping class="com.coolweather.android.db.Province"/>
 *         <mapping class="com.coolweather.android.db.City"/>
 *         <mapping class="com.coolweather.android.db.County"/>
 *     </list>
 * </litepal>
 *      <dbname>此标签用于指定数据库名</dbname>
 *      <version>用于指定数据库版本号</version>
 *      <list>用于指定所有的映射类型</list>
 *      <mapping>用于声明我们要配置的映射模型类</mapping>
 */
public class Province extends LitePalSupport {
    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
