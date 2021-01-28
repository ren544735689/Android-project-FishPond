package com.example.android_forest_app.beans;

public class Note {
    public final long id;       //事件在数据库中存储条目对应的唯一id
    private String deadline;    //事件的截止日期
    private String scheduled;   //事件的记录日期
    private String state;       //事件的状态,分为Todo,Done和None
    private String caption;     //事件的标题
    private String time;        //事件的设定时间

    public Note(long id) {
        this.id = id;
    }
    public long getID() { return id; }
    public String getDeadline(){return deadline;}
    public void setDeadline(String deadline){this.deadline=deadline; }
    public String getScheduled(){return scheduled;}
    public void setScheduled(String scheduled){this.scheduled=scheduled;}
    public String getState(){return state;}
    public void setState(String state){this.state=state;}
    public void setCaption(String caption){this.caption=caption;}
    public String getCaption() { return caption; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
