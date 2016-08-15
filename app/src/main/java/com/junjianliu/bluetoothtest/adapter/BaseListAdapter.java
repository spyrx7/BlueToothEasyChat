package com.junjianliu.bluetoothtest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by junjianliu
 * on 16/7/24
 * email:spyhanfeng@qq.com
 */
public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<RecyclerHolder> {
    protected List<T> realDatas;
    protected int mItemLayoutId;
    protected boolean isScrolling;
    protected Context context;
    private int pos;
    private AdapterView.OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(View view, Object data, int position);
    }
    public BaseListAdapter(RecyclerView v, Collection<T> datas, int itemLayoutId) {
        if (datas == null) {
            realDatas = new ArrayList<>();
        } else if (datas instanceof List) {
            realDatas = (List<T>) datas;
        } else {
            realDatas = new ArrayList<>(datas);
        }
        mItemLayoutId = itemLayoutId;
        context = v.getContext();
        v.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isScrolling = !(newState == RecyclerView.SCROLL_STATE_IDLE);
                if (!isScrolling) {
                    notifyDataSetChanged();
                }
            }
        });
    }
    /**
     * Recycler适配器填充方法
     *
     * @param holder      viewholder
     * @param item        javabean
     * @param isScrolling RecyclerView是否正在滚动
     */
    public abstract void convert(RecyclerHolder holder, T item, int position, boolean isScrolling);
    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(mItemLayoutId, parent, false);
        return new RecyclerHolder(root);
    }
    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        this.pos = position;
        convert(holder, realDatas.get(position), position, isScrolling);
    }
    @Override
    public int getItemCount() {
        return realDatas.size();
    }

    public BaseListAdapter<T> refresh(Collection<T> datas) {
        if (datas == null) {
            realDatas = new ArrayList<>();
        } else if (datas instanceof List) {
            realDatas = (List<T>) datas;
        } else {
            realDatas = new ArrayList<>(datas);
        }
        return this;
    }
    public void add(T t){
        if(t != null){
            this.realDatas.add(t);
        }
    }

    public void setDatas(List<T> datas){
        this.realDatas = realDatas;
    }
    public void add(T t,int postion){
        this.realDatas.set(postion, t);
    }
    public void adds(List<T> T){
        this.realDatas.addAll(T);
    }
    public void remove(int postion){
        this.realDatas.remove(postion);
    }
    public void clear(){
        this.realDatas.clear();
    }

}
