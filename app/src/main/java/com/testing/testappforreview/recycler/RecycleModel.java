package com.testing.testappforreview.recycler;

public class RecycleModel {
    private String appName, img, messageTXT;
    private long mTime;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMessageTXT() {
        return messageTXT;
    }

    public void setMessageTXT(String messageTXT) {
        this.messageTXT = messageTXT;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public long getMTime() {
        return mTime;
    }

    public void setMTime(long mTime) {
        this.mTime = mTime;
    }
}

