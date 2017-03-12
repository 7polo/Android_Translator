package com.apologizebao.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.apologizebao.Tools;
import com.apologizebao.bean.ResultBean;
import com.apologizebao.bean.Userbean;
import com.apologizebao.net.NetConnection;
import com.apologizebao.traslator.R;
import com.apologizebao.ui.ExpandListView;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    //控件
    private LinearLayout content_layout, page_layout;
    private EditText wordInput;
    private TextView us_phonetic, uk_phonetic;
    private ListView explansListView, webListView;
    private ImageButton uk_voice, us_voice, queryButton;
    private LinearLayout activity_main;
    private PopupWindow win;

    private Point point = new Point();

    private float centerY;

    private MediaPlayer player;

    private NetConnection connection = null;
    private volatile ResultBean resultBean = null;
    private volatile boolean singleFlag = false; //进行按钮阻塞
    private volatile boolean playFlag = false; //为true时，发音按钮才可以点击
    private boolean firstInit = true;

    //数据适配器
    private ArrayAdapter<String> basicExpAdapter;
    private SimpleAdapter webExpAdapter;
    private ArrayList<HashMap<String, String>> webDataList = new ArrayList<>();
    private HashMap<String, String> webDataMap;


    WindowManager.LayoutParams params;

    //
    public void showConfig() {
        win = new ConfigWindow(this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        win.showAtLocation(findViewById(R.id.activity_main), Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        params = getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getWindow().setAttributes(params);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        win.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Userbean bean = Tools.readConfig(getApplicationContext());
        connection = NetConnection.getInstance();
        connection.setConfig(bean);
        findView();

        basicExpAdapter = new ArrayAdapter<String>(this, R.layout.basic_item, new ArrayList<String>());
        explansListView.setAdapter(basicExpAdapter);
        webExpAdapter = new SimpleAdapter(this, webDataList, R.layout.web_item, new String[]{"key", "value"}, new int[]{R.id.webKey, R.id.webValue});
        webListView.setAdapter(webExpAdapter);

        player = new MediaPlayer();
        resultBean = new ResultBean();

        //事件监听，回车键即开始搜索
        wordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (wordInput.getText().toString().trim().length() == 0) {
                    resetInitData();
                }
                if (wordInput.getText().toString().contains("\n")) {

                    wordInput.setText(wordInput.getText().toString().replaceAll("\\n", ""));
                    wordInput.setSelection(wordInput.getText().toString().length());
                    doQuery(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        resetInitData();
        queryButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showConfig();
                return true;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        //只运行一次
        if (firstInit) {
            firstInit = false;
            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(300);
                        WindowManager winManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        centerY = (winManager.getDefaultDisplay().getHeight() - 2 * page_layout.getY()) / 3;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resetInitData();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * 绑定View
     */
    private void findView() {
        wordInput = (EditText) findViewById(R.id.wordInput);
        us_phonetic = (TextView) findViewById(R.id.us_phonetic);
        uk_phonetic = (TextView) findViewById(R.id.uk_phonetic);
        explansListView = (ExpandListView) findViewById(R.id.explansListView);
        webListView = (ExpandListView) findViewById(R.id.webListView);
        uk_voice = (ImageButton) findViewById(R.id.uk_voice);
        us_voice = (ImageButton) findViewById(R.id.us_voice);
        content_layout = (LinearLayout) findViewById(R.id.content_layout);
        page_layout = (LinearLayout) findViewById(R.id.page_layout);
        activity_main = (LinearLayout) findViewById(R.id.activity_main);
        queryButton = (ImageButton) findViewById(R.id.queryButton);
    }

    //点击按钮查询
    public void doQuery(View v) {
        connection.setQuery("" + wordInput.getText().toString().trim());

        if (connection.getQuery().trim().equals(""))
            return;

        //如果查询内容没变，则不进行请求
        if (resultBean != null && connection.getQuery().equals(resultBean.getQuery())) {
            return;
        }

        //查询入口
        if (!singleFlag) {

            Toast.makeText(getApplicationContext(), "开始查询", Toast.LENGTH_SHORT).show();
            singleFlag = true;
            //数据初始化
            resetInitData();
            //执行查询操作,网络需要开启线程
            new Thread() {
                @Override
                public void run() {
                    resultBean = connection.doQuery();

                    if (resultBean != null && resultBean.getErrorCode() == 0) {
                        showData(resultBean);
                    } else {
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                        } finally {
                            singleFlag = false;
                            //显示网络连接失败提示信息
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }.start();

        } else {
            Toast.makeText(getApplicationContext(), "查询中，请稍等...", Toast.LENGTH_SHORT).show();
        }
    }

    public void playVoice(View view) {
        //返回数据才允许播放语音
        if (resultBean != null && playFlag) {

            ImageButton clickedView = (ImageButton) view;
            int type = clickedView == us_voice ? 1 : 2;
            try {
                player.reset();
                player.setDataSource(this, Uri.parse(connection.getVoiceURI() + "?audio=" + resultBean.getQuery() + "&type=" + type));
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据初始化
     */
    private void resetInitData() {

        resetButtonLocation();
        content_layout.setVisibility(View.INVISIBLE);
        resultBean = null;
        //清空适配器中的数据
        basicExpAdapter.clear();
        basicExpAdapter.notifyDataSetChanged();
        webDataList.clear();
        webExpAdapter.notifyDataSetChanged();

        us_phonetic.setText("");
        uk_phonetic.setText("");
        playFlag = false;
    }

    /**
     * 通过UI线程更新数据显示
     */
    private void showData(final ResultBean bean) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bean.getBasic() != null) {
                    moveButtonLocation();
                    content_layout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "查询到结果....", Toast.LENGTH_SHORT).show();
                    //发音为null,不显示null
                    if (bean.getBasic().getUk_phonetic() != null)
                        uk_phonetic.setText(bean.getBasic().getUk_phonetic());
                    if (bean.getBasic().getUs_phonetic() != null)
                        us_phonetic.setText(bean.getBasic().getUs_phonetic());

                    //适配器数据
                    basicExpAdapter.addAll(resultBean.getBasic().getExplains());
                    basicExpAdapter.notifyDataSetChanged();
                    if (bean.getWeb() != null) {
                        for (ResultBean.WebEntity webEntity : bean.getWeb()) {
                            webDataMap = new HashMap<String, String>();
                            webDataMap.put("key", webEntity.getKey());
                            webDataMap.put("value", webEntity.getValues());
                            webDataList.add(webDataMap);
                        }
                        webExpAdapter.notifyDataSetChanged();
                    }
                    playFlag = true; //查询到结果才可以发音
                } else {
                    Toast.makeText(getApplicationContext(), "未查到结果", Toast.LENGTH_LONG).show();
                }
                singleFlag = false;
            }
        });
    }


    private void moveButtonLocation() {
        ObjectAnimator.ofFloat(page_layout, "translationY", page_layout.getTranslationY(), 0).setDuration(1000).start();
    }

    private void resetButtonLocation() {

        ObjectAnimator.ofFloat(page_layout, "translationY", page_layout.getTranslationY(), centerY).setDuration(600).start();

    }


}
