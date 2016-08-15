package com.junjianliu.bluetoothtest.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.junjianliu.bluetoothtest.R;
import com.junjianliu.bluetoothtest.bean.BluetoothMsg;
import com.junjianliu.bluetoothtest.ui.ChatActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by junjianliu
 * on 16/8/15
 * email:spyhanfeng@qq.com
 */
public class DeviceListAdapter extends BaseListAdapter<BluetoothDevice> {

    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static BluetoothSocket btSocket;
    private TextView mTvStauts;

    public DeviceListAdapter(RecyclerView v, Collection<BluetoothDevice> datas, int itemLayoutId) {
        super(v, datas, itemLayoutId);
        init();
    }

    @Override
    public void convert(RecyclerHolder holder, final BluetoothDevice item, int position, boolean isScrolling) {

        mTvStauts = holder.getView(R.id.item_stauts);

        if(item.getBondState() == BluetoothDevice.BOND_BONDED){
            mTvStauts.setText("已绑定");
        }

        holder.setText(R.id.item_name,item.getName());
        holder.getView(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(item.getAddress());
                try {
                    Boolean returnValue = false;
                    if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                        Method createBondMethod = BluetoothDevice.class
                                .getMethod("createBond");
                        Log.e("BlueToothTestActivity", "开始配对");
                        returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);

                    }else if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                        connect(bluetoothDevice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.getView(R.id.layout).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                BluetoothMsg.BlueToothAddress = item.getAddress();
                BluetoothMsg.lastblueToothAddress = item.getAddress();
                BluetoothMsg.BlueToothName = item.getName();
                context.startActivity(new Intent(context, ChatActivity.class));

                return false;
            }
        });


    }

    private void init(){
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(searchDevices, intent);
    }

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e(keyName, String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device = null;
            // 搜索设备时，取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    String str = "未配对|" + device.getName() + "|"
                            + device.getAddress();
                   /* if (lstDevices.indexOf(str) == -1)// 防止重复添加
                        lstDevices.add(str); // 获取设备名称和mac地址
                    notifyDataSetChanged();*/
                }
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.e("BlueToothTestActivity", "正在配对......");
                        mTvStauts.setText("正在配对...");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e("BlueToothTestActivity", "完成配对");
                        mTvStauts.setText("完成配对");
                        connect(device);//连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.e("BlueToothTestActivity", "取消配对");
                        mTvStauts.setText("取消配对");
                    default:
                        break;
                }
            }

        }
    };

    private void connect(BluetoothDevice bluetoothDevice) {
        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            Log.e("BlueToothTestActivity", "开始连接...");
            mTvStauts.setText("开始配对");
            btSocket.connect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

