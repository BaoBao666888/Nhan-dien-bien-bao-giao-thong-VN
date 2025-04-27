package com.example.nhandienbienbao.Models;

public class ThongKeItem {
    public String time;
    public String label;
    public String accuracy;
    public String imageName;

    public ThongKeItem(String time, String label, String accuracy, String imageName) {
        this.time = time;
        this.label = label;
        this.accuracy = accuracy;
        this.imageName = imageName;
    }
}
