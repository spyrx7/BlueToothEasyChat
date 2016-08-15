package com.junjianliu.bluetoothtest.bean;

/**
 * Created by junjianliu
 * on 16/8/15
 * email:spyhanfeng@qq.com
 */
public class BluetoothMsg {
    /**
     * 蓝牙连接类型
     * @author Andy
     *
     */
    public enum ServerOrCilent{
        NONE,
        SERVICE,
        CILENT
    };
    //蓝牙连接方式
    public static ServerOrCilent serviceOrCilent = ServerOrCilent.NONE;
    //
    public static String BlueToothName = null;
    //连接蓝牙地址
    public static String BlueToothAddress = null,lastblueToothAddress=null;
    //通信线程是否开启
    public static boolean isOpen = false;
}
