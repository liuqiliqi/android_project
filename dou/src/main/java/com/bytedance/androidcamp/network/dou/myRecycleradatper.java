package com.bytedance.androidcamp.network.dou;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class myRecycleradatper extends RecyclerView.Adapter<myRecycleradatper.MyViewHolder> {
    private List<filelist> mDatas;
    private Context mContext;
    private LayoutInflater inflater;
    private myRecycleradatper recycleAdapter;
    Map<String, Integer> drawableMap = new HashMap<>();

    public myRecycleradatper(Context context, List<filelist> datas){
        this. mContext=context;
        this. mDatas=datas;
        inflater= LayoutInflater. from(mContext);
        drawableMap.put("paris", R.drawable.paris);
        drawableMap.put("beijing",R.drawable.beijing);
        drawableMap.put("london",R.drawable.london);
    }
    @Override
    public int getItemCount() {
        return mDatas.size();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_view,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        filelist da=mDatas.get(position);
        if(position%2==0){
            holder.v.setBackgroundColor(0xFFAEB9);
        } else {
            holder.v.setBackgroundColor(Color.WHITE);
        }

        holder.tv.setText(da.getName());
        holder.ima.setImageResource(drawableMap.get(da.getName()));
        holder.bt.setId(2000+position);
        holder.bt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            public void onClick(View v) {
                Toast.makeText(mContext, "deleted"+(v.getId()-2000), Toast.LENGTH_LONG).show();
                mDatas.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mDatas.size() - position);
            }
        });
        if(position<3) {
            holder.tv.setTextColor(Color.RED);
            holder.re.setVisibility(View.VISIBLE);
        } else {
            holder.tv.setTextColor(Color.BLACK);
            holder.re.setVisibility(View.INVISIBLE);
        }
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView ima;
        Button bt;
        View v;
        ImageView re;
        public MyViewHolder(View view) {
            super(view);
            tv=(TextView) view.findViewById(R.id.txt_xs);
            ima=(ImageView) view.findViewById(R.id.imageView1);
            bt=(Button) view.findViewById(R.id.button);
            re=(ImageView)view.findViewById(R.id.imageView);
            v=view;
        }
    }
}