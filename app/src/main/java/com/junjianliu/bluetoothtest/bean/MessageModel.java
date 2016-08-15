package com.junjianliu.bluetoothtest.bean;

/**
 * Created by junjianliu
 * on 16/8/15
 * email:spyhanfeng@qq.com
 */
public class MessageModel {

    private int type;          // 0 提示消息   1 我的回复  3 对方的回复
    private String msg;

    private String name;      // 用于保存对方蓝牙名

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
