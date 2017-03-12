package com.apologizebao;

import android.content.Context;
import android.util.Log;

import com.apologizebao.bean.Userbean;
import com.apologizebao.net.NetConnection;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by apologizebao on 17-3-11.
 */

public class Tools {

    /**
     * 将Userbean的json形式
     * @param context
     * @param userbean
     */
    public static void saveConfig(Context context, Userbean userbean) {
        try {
            File parent = context.getApplicationContext().getFilesDir();
            File cfg = new File(parent.getPath(), "config.json");
            if (!cfg.exists())
                cfg.createNewFile();

            Gson gson = new Gson();
            String json = gson.toJson(userbean, userbean.getClass());
            FileWriter writer = new FileWriter(cfg);
            writer.write(json);
            writer.close();
            NetConnection.getInstance().setConfig(userbean);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Userbean readConfig(Context context) {
        Userbean bean = null;
        File parent = context.getApplicationContext().getFilesDir();
        File cfg = new File(parent.getPath(), "config.json");
        if (cfg.exists()) {
            Gson gson = new Gson();
            try {
                bean = gson.fromJson(new FileReader(cfg), Userbean.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (bean == null) {

            bean = new Userbean();
            bean.setKey("");
            bean.setKeyfrom("");
            saveConfig(context, bean);
        }
        return bean;
    }
}
