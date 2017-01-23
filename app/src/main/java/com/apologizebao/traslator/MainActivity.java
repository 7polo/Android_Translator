package com.apologizebao.traslator;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apologizebao.bean.ResultBean;
import com.apologizebao.net.NetConnection;
import com.apologizebao.ui.ExpandListView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    //控件
    private EditText wordInput;
    private TextView us_phonetic, uk_phonetic;
    private ListView explansListView, webListView;
    private ImageButton uk_voice, us_voice;

    private MediaPlayer player;

    private NetConnection connection = null;
    private volatile ResultBean resultBean = null;
    private volatile boolean singleFlag = false; //进行按钮阻塞
    private volatile boolean playFlag = false; //为true时，发音按钮才可以点击


    //数据适配器
    private ArrayAdapter<String> basicExpAdapter, webExpAdapter;

    //点击按钮查询
    public void doQuery(View v) {
        connection.setQuery("" + wordInput.getText().toString().trim());

        //如果查询内容没变，则不进行请求
        if (resultBean != null && connection.getQuery().equals(resultBean.getQuery())) {
            return;
        }

        if (!connection.getQuery().equals("") && !singleFlag) {

            Toast.makeText(getApplicationContext(), "开始查询", Toast.LENGTH_SHORT).show();
            singleFlag = true;
            //数据初始化
            resetInitData();
            //执行查询操作,网络需要开启线程
            new Thread() {
                @Override
                public void run() {
                    resultBean = connection.doQuery();

                    if (resultBean.getErrorCode() == 0) {
                        showData(resultBean);
                    }
                    try {
                        sleep(1500);
                    } catch (InterruptedException e) {

                    } finally {
                        singleFlag = false;
                    }
                }
            }.start();

        } else {
            Toast.makeText(getApplicationContext(), "查询任务中，请稍等...", Toast.LENGTH_SHORT).show();
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
    }

    /**
     * 数据初始化
     */
    private void resetInitData() {

        resultBean = null;
        basicExpAdapter.clear();
        basicExpAdapter.notifyDataSetChanged();
        us_phonetic.setText("");
        uk_phonetic.setText("");
        playFlag = false;
    }


    /**
     * 通过UI线程更新数据显示
     */
    private void showData(final ResultBean bean) {
        if (bean.getErrorCode() == 0 && bean.getBasic() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "查询到结果，正在刷新数据....", Toast.LENGTH_LONG).show();
                    uk_phonetic.setText(bean.getBasic().getUk_phonetic());
                    us_phonetic.setText(bean.getBasic().getUs_phonetic());
                    basicExpAdapter.addAll(resultBean.getBasic().getExplains());
                    basicExpAdapter.notifyDataSetChanged();

                    playFlag = true; //查询到结果才可以发音
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = NetConnection.getInstance();
        findView();

        basicExpAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        explansListView.setAdapter(basicExpAdapter);
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


//        webExpAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultBean.web.getValue());
    }


}
