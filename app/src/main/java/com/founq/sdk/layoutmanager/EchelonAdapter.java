package com.founq.sdk.layoutmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by ring on 2019/7/17.
 */
public class EchelonAdapter extends RecyclerView.Adapter<EchelonAdapter.MyHolder> {

    private Context mContext;

    private int[] icons = {R.drawable.header_icon_1,R.drawable.header_icon_2,R.drawable.header_icon_3,R.drawable.header_icon_4,
            R.drawable.header_icon_1,R.drawable.header_icon_2,R.drawable.header_icon_3,R.drawable.header_icon_4};
    private int[] bgs = {R.drawable.bg_1,R.drawable.bg_2,R.drawable.bg_3,R.drawable.bg_4,
            R.drawable.bg_1,R.drawable.bg_2,R.drawable.bg_3,R.drawable.bg_4};
    private String[] nickNames = {"左耳近心","凉雨初夏","稚久九栀","半窗疏影",
            "左耳近心","凉雨初夏","稚久九栀","半窗疏影"};
    private String[] descs = {
            "回不去的地方叫故乡 没有根的迁徙叫流浪...",
            "人生就像迷宫，我们用上半生找寻入口，用下半生找寻出口",
            "原来地久天长，只是误会一场",
            "不是故事的结局不够好，而是我们对故事的要求过多",
            "只想优雅转身，不料华丽撞墙",
            "回不去的地方叫故乡 没有根的迁徙叫流浪...",
            "人生就像迷宫，我们用上半生找寻入口，用下半生找寻出口",
            "原来地久天长，只是误会一场",
            "不是故事的结局不够好，而是我们对故事的要求过多",
            "只想优雅转身，不料华丽撞墙"
    };

    public EchelonAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_echelon, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        myHolder.mIntroduceText.setText(descs[i]);
        myHolder.mNickNameText.setText(nickNames[i]);
        Glide.with(mContext)
                .load(bgs[i])
                .into(myHolder.mBGImg);
        Glide.with(mContext)
                .load(icons[i])
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(myHolder.mHeadImg);
    }

    @Override
    public int getItemCount() {
        return 8;
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private ImageView mHeadImg;
        private ImageView mBGImg;
        private TextView mNickNameText;
        private TextView mIntroduceText;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mHeadImg = itemView.findViewById(R.id.iv_head);
            mBGImg = itemView.findViewById(R.id.iv_bg);
            mNickNameText = itemView.findViewById(R.id.tv_nickname);
            mIntroduceText = itemView.findViewById(R.id.tv_introduce);
        }
    }
}
