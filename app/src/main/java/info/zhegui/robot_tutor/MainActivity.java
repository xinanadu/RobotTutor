package info.zhegui.robot_tutor;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;


public class MainActivity extends ActionBarActivity {

    private LinkedList<Sentence> listSentence = new LinkedList<Sentence>();

    private TextView tv;

    private final int WHAT_SHOW_TEXT = 101;

    private Sentence lastSentence;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SHOW_TEXT:
                    if (tv != null) {
                        tv.setText("");
                        for (Sentence sentence : listSentence) {
                            String lineSeperator="\n";
                            if(lastSentence!=null && !TextUtils.equals(sentence.speaker, lastSentence.speaker)){
                                lineSeperator="\n\n";
                            }
                            tv.append(sentence.speaker + ": " + sentence.content + lineSeperator);

                            lastSentence=sentence;
                        }
                    }
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);

        loadDialog();
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
                        log("-->"+str);
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
