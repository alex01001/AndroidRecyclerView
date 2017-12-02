package com.example.android.priceviewer;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter <PriceAdapter.MyViewHolder>{

    private LayoutInflater inflater;
    List<PriceListItem> data = Collections.emptyList();

    final private PriceItemClickListener onClickListener;


    public interface PriceItemClickListener {

        void onPriceItemClick(int ClickedItemIndex);

    }

    public PriceAdapter (Context context, PriceItemClickListener listener){
        inflater = LayoutInflater.from(context);
        onClickListener = listener;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.price_listitem,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PriceListItem current = data.get(position);
        holder.itemImg.setImageResource(current.img);
        holder.itemDate.setText(current.date);
        holder.itemPrice.setText(current.price);
        holder.itemChange.setText(current.change);

    }

    @Override
    public int getItemCount() {
        if(data==null) return 0;
        return data.size();
    }

    public void setPriceData (List<PriceListItem> pData){
        this.data = pData;
        notifyDataSetChanged();

    }

    class MyViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private TextView itemDate;
        private TextView itemPrice;
        private TextView itemChange;
        private ImageView itemImg;

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            onClickListener.onPriceItemClick(clickedPosition);
        }

        public MyViewHolder(View itemView) {
            super(itemView);
            itemImg = (ImageView) itemView.findViewById(R.id.list_icon);
            itemDate = (TextView) itemView.findViewById(R.id.list_date);
            itemPrice = (TextView) itemView.findViewById(R.id.list_price);
            itemChange = (TextView) itemView.findViewById(R.id.list_change);
            itemView.setOnClickListener(this);
        }
    }
}
