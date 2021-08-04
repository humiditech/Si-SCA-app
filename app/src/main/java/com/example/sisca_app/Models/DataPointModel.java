package com.example.sisca_app.Models;

import com.jjoe64.graphview.series.DataPointInterface;

public class DataPointModel {
    long xValue;
    int yValue;

    public DataPointModel() {
    }

    public DataPointModel(long xValue, int yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public long getxValue() {
        return xValue;
    }

    public int getyValue() {
        return yValue;
    }

}
