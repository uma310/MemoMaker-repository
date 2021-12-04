package com.example.memomaker;



import java.util.Date;

import io.realm.RealmObject;

public class SampleModel extends RealmObject{
    private int Id;
    private String text;
    private String date;
    private String time;
    private String image;

    public  int getId(){return this.Id;}

    public void setId(int Id){this.Id = Id;}

    public String getText(){
        return this.text;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getDate(){
        return this.date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getTime(){ return this.time; }

    public void setTime(String time){this.time = time;}

    public String getImage(){ return this.image;}

    public void setImage(String image){this.image = image;}
}
