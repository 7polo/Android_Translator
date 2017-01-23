package com.apologizebao.net;

import com.apologizebao.bean.ResultBean;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by apologize on 2016/11/13.
 */
public class NetConnection {
    private ResultBean resultBean;  //保存结果
    private String queryURI = "http://fanyi.youdao.com/openapi.do";

    public String getVoiceURI() {
        return voiceURI;
    }

    public final String voiceURI = "http://dict.youdao.com/dictvoice";

    private String keyform = "javaTranslator";
    private String key = "332136996";
    private String doctype = "json";
    private String query = "";
    private StringBuilder requestString;

    //网络连接
    private HttpURLConnection connection;
    private URL url;
    private Gson gson;

    public String getQuery() {
        return query;
    }

    private static NetConnection instance;

    public static NetConnection getInstance() {
        if (instance == null) {
            synchronized (NetConnection.class) {
                if (instance == null)
                    instance = new NetConnection();
            }
        }
        return instance;
    }

    //TODO 最好从文件读取，后期修改
    private NetConnection() {
        requestString = new StringBuilder(queryURI + "?keyfrom=" +
                keyform + "&key=" + key + "&type=data&doctype=" + doctype + "&version=1.1&q=");
        System.out.println(requestString);
    }

    public ResultBean doQuery() {
        try {
            url = new URL(requestString + URLEncoder.encode(query.trim(), "utf-8"));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(500);
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "utf-8"); //获得响应信息

            return resultBean = adaptor(reader);

        } catch (IOException e) {
            resultBean = new ResultBean();
            resultBean.setErrorCode(-1); //-1 连接失败
        } finally {
            connection.disconnect();
        }
        return resultBean;
    }

    public ResultBean doQuery(String query) {
        this.query = query;
        return doQuery();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * 解决适配问题，因为json中含有 - 字符，无法作为变量名,此方法将它转换为 _
     *
     * @param reader
     * @return ResultBean对象
     */
    private ResultBean adaptor(InputStreamReader reader) {

        StringBuffer dataBuffer = new StringBuffer();
        char data[] = new char[1024];
        int size = 0;
        try {
            while ((size = reader.read(data)) != -1) {
                dataBuffer.append(data, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        gson = new Gson();
        return gson.fromJson(dataBuffer.toString().replaceAll("-", "_"), ResultBean.class);
    }

}