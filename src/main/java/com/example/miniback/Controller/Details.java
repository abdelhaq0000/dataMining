package com.example.miniback.Controller;

import java.util.List;

public class Details {
    public List<List<String>> trainData;
    public List<List<String>> testData;
    public int countTRUS;
    public int countFALS;

    public Details(List<List<String>> trainData, List<List<String>> testData, int countTRUS, int countFALS) {
        this.trainData = trainData;
        this.testData = testData;
        this.countTRUS = countTRUS;
        this.countFALS = countFALS;
    }
}
