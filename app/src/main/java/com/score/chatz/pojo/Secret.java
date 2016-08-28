package com.score.chatz.pojo;

/**
 * Created by Lakmal on 7/31/16.
 */

import com.score.senzc.pojos.User;
public class Secret {
    private String text;
    private String image;
    private User sender;
    private User receiver;
    private boolean isDelete;
    private Long timeStamp;
    private String id;
    private String imageThumbnail;
    private String sound;

    public Secret(String text, String image, String thumb, User sender, User receiver) {
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.image = image;
        this.imageThumbnail = thumb;
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getImage() {
        return image;
    }

    public String getThumbnail() {
        return imageThumbnail;
    }

    public void setSound(String _sound){
        this.sound = _sound;
    }

    public String getSound(){
        return sound;
    }

    public void setDelete(boolean val){
        isDelete = val;
    }

    public void setTimeStamp(Long ts){
        timeStamp = ts;
    }

    public boolean isDelete(){
        return isDelete;
    }

    public Long getTimeStamp(){
        return timeStamp;
    }

    public void setID(String val){
        id = val;
    }

    public String getID(){
        return id;
    }

    @Override
    public String toString() {
        return ("Secret Text:"+this.getText()+
                " Secret Sender: "+ this.getSender().getUsername() +
                " Secret Receiver: "+ this.getReceiver().getUsername() +
                " Secret Image : " + this.getImage());
    }

}
