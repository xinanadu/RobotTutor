package info.zhegui.robot_tutor;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.voicerecognition.android.VoiceRecognitionConfig;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DialogRecognitionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;


public class MainActivity extends ActionBarActivity {

    private BaiduASRDigitalDialog mDialog = null;

    private DialogRecognitionListener mRecognitionListener;
    private int mCurrentTheme = Config.DIALOG_THEME;
    private LinkedList<Sentence> listSentence = new LinkedList<Sentence>();

    private TextView tv, tvNoMatch;

    private final int WHAT_SHOW_TEXT = 101, WHAT_FILTER_TEXT = 102;

    private Sentence lastSentence;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SHOW_TEXT:
                    showMyDialog(null);
                    break;

                case WHAT_FILTER_TEXT:

                    if (msg.obj == null) {
                        showNoMatch();
                    } else {
                        String str = (String) msg.obj;
    showMyDialog(str);
                    }
                    break;
            }

        }
    };

    private void showNoMatch() {
        Toast.makeText(MainActivity.this, "no match",Toast.LENGTH_SHORT).show();
    }

    private void showMyDialog(String strFilter) {
        if (tv != null) {
            lastSentence=null;
            tv.setText("");
            for (Sentence sentence : listSentence) {
                String lineSeperator = "";
                if (lastSentence != null) {
                    if (!TextUtils.equals(sentence.speaker, lastSentence.speaker)) {
                        lineSeperator = "\n\n";
                    } else {
                        lineSeperator = "\n";
                    }
                }
                String str = lineSeperator + "[" + sentence.speaker + "]: " + sentence.content;
                log("-->" + str);
                SpannableStringBuilder style = new SpannableStringBuilder(str);
                int start = 0;
                int end = str.indexOf("]") + 2;

                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#C6BAA1")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            style.setSpan(new ForegroundColorSpan(Color.RED),7,9, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                if(!TextUtils.isEmpty(strFilter)) {
                    int startStr=str.toLowerCase().indexOf(strFilter.toLowerCase());
                    log("startStr:"+startStr);
                    int endStr=startStr+strFilter.length();
                    log("endStr:"+endStr);
                    if(startStr>=0)
                    style.setSpan(new ForegroundColorSpan(Color.parseColor("#66cc33")), startStr, endStr, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                tv.append(style);

                lastSentence = sentence;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        tvNoMatch = (TextView) findViewById(R.id.tv_no_match);

        loadDialog();


        mRecognitionListener = new DialogRecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> rs = results != null ? results
                        .getStringArrayList(RESULTS_RECOGNITION) : null;
                for (int i = 0; rs != null && i < rs.size(); i++) {
                    log("-->" + rs.get(i));
                }

                String str = null;
                if (rs != null && rs.size() > 0) {
                    str = rs.get(0);
                }
                Message msg = mHandler.obtainMessage(WHAT_FILTER_TEXT, str);
                msg.sendToTarget();
            }
        };

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenToVoice();
            }
        });
    }

    private void listenToVoice() {
        log("listToVoice()");
        if (mDialog == null || mCurrentTheme != Config.DIALOG_THEME) {
            mCurrentTheme = Config.DIALOG_THEME;
            if (mDialog != null) {
                mDialog.dismiss();
            }
            Bundle params = new Bundle();
            params.putString(BaiduASRDigitalDialog.PARAM_API_KEY, Constants.API_KEY);
            params.putString(BaiduASRDigitalDialog.PARAM_SECRET_KEY, Constants.SECRET_KEY);
            params.putInt(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, Config.DIALOG_THEME);
            mDialog = new BaiduASRDigitalDialog(MainActivity.this, params);
            mDialog.setDialogRecognitionListener(mRecognitionListener);
        }
        mDialog.getParams().putInt(BaiduASRDigitalDialog.PARAM_PROP, Config.CURRENT_PROP);
        mDialog.getParams().putString(BaiduASRDigitalDialog.PARAM_LANGUAGE,
                VoiceRecognitionConfig.LANGUAGE_ENGLISH);
        Log.e("DEBUG", "Config.PLAY_START_SOUND = " + Config.PLAY_START_SOUND);
        mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_START_TONE_ENABLE, Config.PLAY_START_SOUND);
        mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_END_TONE_ENABLE, Config.PLAY_END_SOUND);
        mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_TIPS_TONE_ENABLE, Config.DIALOG_TIPS_SOUND);
        mDialog.show();
    }

    private void loadDialog() {
        new Thread() {
            @Override
            public void run() {
                Resources res = MainActivity.this.getResources();
                InputStream in = null;
                BufferedReader br = null;
                try {
                    in = res.openRawResource(R.raw.dialog);
                    String str;
                    br = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    listSentence.clear();
                    ;
                    while ((str = br.readLine()) != null) {
                        log("-->" + str);
                        try {
                            String[] arr = str.split(":");
                            Sentence sentence = new Sentence();
                            sentence.speaker = arr[0];
                            sentence.content = arr[1];
                            listSentence.add(sentence);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ;
                        }
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
                log("listSentence.size():" + listSentence.size());
                mHandler.sendEmptyMessage(WHAT_SHOW_TEXT);
            }
        }.start();
        ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class Sentence {
        String speaker;
        String content;
    }

    private void log(String msg) {
        Log.d(this.getClass().getSimpleName(), msg);
    }
}
