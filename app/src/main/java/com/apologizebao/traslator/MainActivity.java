package com.apologizebao.traslator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apologizebao.bean.ResultBean;
import com.apologizebao.net.NetConnection;
import com.apologizebao.ui.ExpandListView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    //控件
    private EditText wordInput;  //
    private TextView us_phonetic, uk_phonetic;
    private ListView explansListView, webListView;
    private ImageButton uk_voice, us_voice;

    private NetConnection connection = null;
    private ResultBean resultBean = null;
    private volatile boolean singleFlag = false; //进行按钮阻塞
    private String queryWord = null;

    //数据适配器
    private ArrayAdapter<String> basicExpAdapter, webExpAdapter;

    //点击按钮查询
    public void doQuery(View v) {
        queryWord = "" + wordInput.getText().toString().trim();
        if (!queryWord.equals("") && !singleFlag) {
            //执行查询操作,网络需要开启线程
            Toast.makeText(getApplicationContext(), "开始查询", Toast.LENGTH_SHORT).show();
            resultBean = null;
            basicExpAdapter.clear();
            basicExpAdapter.notifyDataSetChanged();
            singleFlag = true;

            new Thread() {
                @Override
                public void run() {
                    resultBean = connection.doQuery(queryWord);

                    showData(resultBean);
                    if (resultBean.getErrorCode() != 0) {
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finally {
                            singleFlag = false;
                        }
                    }

                }
            }.start();
        } else {
            Toast.makeText(getApplicationContext(), "查询任务中，请稍等...", Toast.LENGTH_SHORT).show();

        }
    }

    public void playVoice(View view){
        Toast.makeText(getApplicationContext(),"播放发音...", Toast.LENGTH_SHORT).show();
        ImageButton clickedView = (ImageButton) view;
        int type = 0;
        if (clickedView == us_voice){
            type = 1;
        }
        else if (clickedView == uk_voice){
            type = 2;
        }
        connection.getVoice(getApplicationContext(),type, resultBean.getQuery());
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
     * 通过UI线程更新数据显示
     */
    private void showData(final ResultBean bean) {
        if (bean.getErrorCode() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "查询到结果，正在刷新数据....", Toast.LENGTH_LONG).show();
                    uk_phonetic.setText(bean.getBasic().getUk_phonetic());
                    us_phonetic.setText(bean.getBasic().getUs_phonetic());
                    basicExpAdapter.addAll(resultBean.getBasic().explains);

                    basicExpAdapter.notifyDataSetChanged();

                    singleFlag = false;
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
//        webExpAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultBean.web.getValue());
    }


}
