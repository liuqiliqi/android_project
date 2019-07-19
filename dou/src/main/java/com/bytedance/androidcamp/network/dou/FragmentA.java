package com.bytedance.androidcamp.network.dou;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FragmentA extends Fragment {
    private List<filelist> mDatas;
    private myRecycleradatper recycleAdapter;
    private Button bt_insert;
    private Button bt_pa;
    Map<String, Integer> drawableMap = new HashMap<>();

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.resou, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatas = new ArrayList<filelist>();
        String zt = "paris";
        filelist fl;
        for (int i = 0; i < 50; i++) {
            zt = "paris";
            fl = new filelist(zt);
            mDatas.add(fl);
            zt = "beijing";
            fl = new filelist(zt);
            mDatas.add(fl);
            zt = "london";
            fl = new filelist(zt);
            mDatas.add(fl);
        }
        recycleAdapter=new myRecycleradatper(getActivity(),mDatas);
        RecyclerView recyclerView=(RecyclerView) getView().findViewById(R.id.mlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity() );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recycleAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
