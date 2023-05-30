package com.example.miniback.Controller;

public class Prediction {
    double Probay ;
    double ProbaN;
    String prediction;

    public Prediction(double probay, double probaN, String prediction) {
        Probay = probay;
        ProbaN = probaN;
        this.prediction = prediction;
    }

    public double getProbay() {
        return Probay;
    }

    public void setProbay(double probay) {
        Probay = probay;
    }

    public double getProbaN() {
        return ProbaN;
    }

    public void setProbaN(double probaN) {
        ProbaN = probaN;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
}
