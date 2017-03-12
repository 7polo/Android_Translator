package com.apologizebao.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apologizebao.Tools;
import com.apologizebao.bean.Userbean;
import com.apologizebao.traslator.R;

/**
 * Created by apologizebao on 17-3-11.
 */

public class ConfigWindow extends PopupWindow {

    private Context mContext;
    private View view;

    private EditText keyEditor, keyfromEditor;
    private ImageButton saveImg;

    public ConfigWindow(Context mContext, View.OnClickListener itemsOnClick) {
        this.mContext = mContext;
        this.view = LayoutInflater.from(mContext).inflate(R.layout.activity_config, null);

        // 设置外部可不点击
        this.setOutsideTouchable(false);
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景git
        this.setBackgroundDrawable(dw);

        this.setAnimationStyle(R.style.take_photo_anim);
        findView();
        showConfigData();
    }

    private void findView(){
        keyEditor = (EditText) view.findViewById(R.id.keyEditor);
        keyfromEditor = (EditText) view.findViewById(R.id.keyfromEditor);
        saveImg = (ImageButton) view.findViewById(R.id.saveButton);

        saveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = keyEditor.getText().toString();
                String keyfrom = keyfromEditor.getText().toString();
                if (TextUtils.isEmpty(key)||TextUtils.isEmpty(keyfrom)){
                }else{
                    Userbean userbean = new Userbean();
                    userbean.setKey(key);
                    userbean.setKeyfrom(keyfrom);
                    Tools.saveConfig(mContext, userbean);
                    Toast.makeText(mContext,"保存配置信息...", Toast.LENGTH_LONG).show();
                    ConfigWindow.this.dismiss();
                }
            }
        });
    }

    private void showConfigData(){
        Userbean userbean = Tools.readConfig(mContext);
        keyEditor.setText(userbean.getKey());
        keyfromEditor.setText(userbean.getKeyfrom());
    }

}