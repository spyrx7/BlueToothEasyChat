package com.junjianliu.bluetoothtest.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.junjianliu.bluetoothtest.R;
import com.junjianliu.bluetoothtest.bean.MessageModel;

import java.util.Collection;

/**
 * Created by junjianliu
 * on 16/8/15
 * email:spyhanfeng@qq.com
 */
public class ChatAdapter extends BaseListAdapter<MessageModel> {

    public ChatAdapter(RecyclerView v, Collection<MessageModel> datas, int itemLayoutId) {
        super(v, datas, itemLayoutId);
    }

    @Override
    public void convert(RecyclerHolder holder, MessageModel item, int position, boolean isScrolling) {

         if(!isScrolling) {
             switch (item.getType()) {
                 case 0:
                     holder.getView(R.id.item_center).setVisibility(View.VISIBLE);
                     holder.setText(R.id.item_center, item.getMsg());
                     break;
                 case 1:
                     holder.getView(R.id.item_left).setVisibility(View.VISIBLE);
                     holder.setText(R.id.item_left, String.format("我: %s", item.getMsg()));
                     break;
                 case 2:
                     holder.getView(R.id.item_right).setVisibility(View.VISIBLE);
                     holder.setText(R.id.item_right, String.format("%s  %s", item.getMsg(), item.getName()));
                     break;
             }
         }

    }
}
