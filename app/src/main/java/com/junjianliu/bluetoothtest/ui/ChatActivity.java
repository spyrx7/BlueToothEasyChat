package com.junjianliu.bluetoothtest.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.junjianliu.bluetoothtest.R;
import com.junjianliu.bluetoothtest.adapter.ChatAdapter;
import com.junjianliu.bluetoothtest.bean.BluetoothMsg;
import com.junjianliu.bluetoothtest.bean.MessageModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by junjianliu
 * on 16/8/15
 * email:spyhanfeng@qq.com
 */
public class ChatActivity extends AppCompatActivity {

    /* 一些常量，代表服务器的名称 */
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private RecyclerView recyclerView;
    private Button sendBtn;
    private Button disconnectBtn;
    private EditText edMsg;

    private ChatAdapter adapter;
    private List<MessageModel> msgList = new ArrayList<MessageModel>();

    private BluetoothServerSocket bluetoothServerSocket = null;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ServerThread startServerThread = null;
    private clientThread clientConnectThread = null;
    private ReadThread readThread = null;

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mContext = this;
        init();
    }

    private void init(){
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(recyclerView,msgList,R.layout.item_chat);
        recyclerView.setAdapter(adapter);
        edMsg = (EditText)findViewById(R.id.MessageText);
        edMsg.clearFocus();

        sendBtn = (Button)findViewById(R.id.btn_msg_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String msgText = edMsg.getText().toString();
                if (msgText.length()>0) {
                    sendMessageHandle(msgText);
                    edMsg.setText("");
                    edMsg.clearFocus();
                    //close InputMethodManager
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edMsg.getWindowToken(), 0);
                }else
                    Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
            }
        });

        disconnectBtn = (Button)findViewById(R.id.btn_disconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT)
                {
                    shutdownClient();
                }
                else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE)
                {
                    shutdownServer();
                }
                BluetoothMsg.isOpen = false;
                BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
                Toast.makeText(mContext, "已断开连接！", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_open){
            startServerThread = new ServerThread();
            startServerThread.start();
            BluetoothMsg.isOpen = true;
        }

        if(id == R.id.action_connect){
            String address = BluetoothMsg.BlueToothAddress;
            if(address != null)
            {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                clientConnectThread = new clientThread();
                clientConnectThread.start();
                BluetoothMsg.isOpen = true;
            }
            else
            {
                Toast.makeText(mContext, "address is null !", Toast.LENGTH_SHORT).show();
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
            if(msg.what==1)
            {
                MessageModel temp = new MessageModel();
                temp.setType(1);
                temp.setName(BluetoothMsg.BlueToothName);
                temp.setMsg((String)msg.obj);
                adapter.add(temp);
            }else if(msg.what == 0){
                MessageModel temp = new MessageModel();
                temp.setType(0);
                temp.setMsg((String)msg.obj);
                adapter.add(temp);
            }else{
                MessageModel temp = new MessageModel();
                temp.setType(2);
                temp.setName(BluetoothMsg.BlueToothName);
                temp.setMsg((String)msg.obj);
                adapter.add(temp);
            }
            //adapter.setDatas(msgList);
            adapter.notifyDataSetChanged();
            //mListView.setSelection(msgList.size() - 1);
        }
    };

    //开启客户端
    private class clientThread extends Thread {
        @Override
        public void run() {
            try {
                //创建一个Socket连接：只需要服务器在注册时的UUID号
                // socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                //连接
                Message msg2 = new Message();
                msg2.obj = "请稍候，正在连接服务器:"+ BluetoothMsg.BlueToothAddress;
                msg2.what = 0;
                LinkDetectedHandler.sendMessage(msg2);

                bluetoothSocket.connect();

                Message msg = new Message();
                msg.obj = "已经连接上服务端！可以发送信息。";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);
                //启动接受数据
                readThread = new ReadThread();
                readThread.start();
            }
            catch (IOException e)
            {
                Log.e("connect", "", e);
                Message msg = new Message();
                msg.obj = "连接服务端异常！断开连接重新试一试。";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);
            }
        }
    };

    //开启服务器
    private class ServerThread extends Thread {
        @Override
        public void run() {

            try {
                    /* 创建一个蓝牙服务器
                     * 参数分别：服务器名称、UUID   */
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                        UUID.fromString(SPP_UUID));

                Log.d("server", "wait cilent connect...");

                Message msg = new Message();
                msg.obj = "请稍候，正在等待客户端的连接...";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);

                    /* 接受客户端的连接请求 */
                bluetoothSocket = bluetoothServerSocket.accept();
                Log.d("server", "accept success !");

                Message msg2 = new Message();
                String info = "客户端已经连接上！可以发送信息。";
                msg2.obj = info;
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg2);
                //启动接受数据
                readThread = new ReadThread();
                readThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    /* 停止服务器 */
    private void shutdownServer() {
        new Thread() {
            @Override
            public void run() {
                if(startServerThread != null)
                {
                    startServerThread.interrupt();
                    startServerThread = null;
                }
                if(readThread != null)
                {
                    readThread.interrupt();
                    readThread = null;
                }
                try {
                    if(bluetoothSocket != null)
                    {
                        bluetoothSocket.close();
                        bluetoothSocket = null;
                    }
                    if (bluetoothServerSocket != null)
                    {
                        bluetoothServerSocket.close();/* 关闭服务器 */
                        bluetoothServerSocket = null;
                    }
                } catch (IOException e) {
                    Log.e("server", "mserverSocket.close()", e);
                }
            };
        }.start();
    }

    /* 停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            @Override
            public void run() {
                if(clientConnectThread!=null)
                {
                    clientConnectThread.interrupt();
                    clientConnectThread= null;
                }
                if(readThread != null)
                {
                    readThread.interrupt();
                    readThread = null;
                }
                if (bluetoothSocket != null) {
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bluetoothSocket = null;
                }
            };
        }.start();
    }


    //发送数据
    private void sendMessageHandle(String msg)
    {
        if (bluetoothSocket == null)
        {
            Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }



        try {
            OutputStream os = bluetoothSocket.getOutputStream();
            os.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        MessageModel temp = new MessageModel();
        temp.setType(1);
        temp.setMsg(msg);
        adapter.add(temp);
        adapter.notifyDataSetChanged();
    }

    //读取数据
    private class ReadThread extends Thread {
        @Override
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = bluetoothSocket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    if( (bytes = mmInStream.read(buffer)) > 0 )
                    {
                        byte[] buf_data = new byte[bytes];
                        for(int i=0; i<bytes; i++)
                        {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = 2;
                        LinkDetectedHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void onResume() {

       /* BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;

        if(BluetoothMsg.isOpen)
        {
            Toast.makeText(mContext, "连接已经打开，可以通信。如果要再建立连接，请先断开！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT)
        {
            String address = BluetoothMsg.BlueToothAddress;
            if(address != null)
            {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                clientConnectThread = new clientThread();
                clientConnectThread.start();
                BluetoothMsg.isOpen = true;
            }
            else
            {
                Toast.makeText(mContext, "address is null !", Toast.LENGTH_SHORT).show();
            }
        }
        else if(BluetoothMsg.serviceOrCilent==BluetoothMsg.ServerOrCilent.SERVICE)
        {
            startServerThread = new ServerThread();
            startServerThread.start();
            BluetoothMsg.isOpen = true;
        }*/
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT)
        {
            shutdownClient();
        }
        else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE)
        {
            shutdownServer();
        }
        BluetoothMsg.isOpen = false;
        BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
    }
}
