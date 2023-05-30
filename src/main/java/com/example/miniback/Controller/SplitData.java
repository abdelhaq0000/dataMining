package com.example.miniback.Controller;

import java.util.List;

public class SplitData {
   public List<List<String>> trainData;
   public List<List<String>> testData;

    public SplitData(List<List<String>> trainData, List<List<String>> testData) {
        this.trainData = trainData;
        this.testData = testData;
    }
}
