package com.coolweather.android.db;

public class Province1 {
    private String provinceEn;
    private String provinceZh;

    public String getProvinceEn() {
        return provinceEn;
    }

    public void setProvinceEn(String provinceEn) {
        this.provinceEn = provinceEn;
    }

    public String getProvinceZh() {
        return provinceZh;
    }

    public void setProvinceZh(String provinceZh) {
        this.provinceZh = provinceZh;
    }

    @Override
    public String toString() {
        return "Province{" +
                "provinceEn='" + provinceEn + '\'' +
                ", provinceZh='" + provinceZh + '\'' +
                '}';
    }
}
