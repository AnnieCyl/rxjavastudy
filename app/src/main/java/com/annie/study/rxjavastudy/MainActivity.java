package com.annie.study.rxjavastudy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv_bbs;
    private TextView tv_control;
    private String[] mCharStr = {"你吃饭了吗？", "今天天气真好呀。", "我中奖啦！", "我们去看电影吧。", "晚上干什么好呢？"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tv_control || view.getId() == R.id.tv_bbs) {
                    int random = (int)(Math.random() * 10) % 5;
                    String newStr = String.format("%s\n%s %s", tv_bbs.getText().toString(), Utils.getCurrentDateTime(), mCharStr[random]);
                    tv_bbs.setText(newStr);
                }
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view.getId() == R.id.tv_control || view.getId() == R.id.tv_bbs) {
                    tv_bbs.setText("");
                }
                return true;
            }
        };

        tv_control = (TextView)findViewById(R.id.tv_control);
        tv_control.setOnClickListener(onClickListener);
        tv_control.setOnLongClickListener(onLongClickListener);

        tv_bbs = (TextView)findViewById(R.id.tv_bbs);
        tv_bbs.setOnClickListener(onClickListener);
        tv_bbs.setOnLongClickListener(onLongClickListener);
        tv_bbs.setGravity(Gravity.LEFT|Gravity.BOTTOM);
        tv_bbs.setLines(8);
        tv_bbs.setMaxLines(8);
        tv_bbs.setMovementMethod(new ScrollingMovementMethod());
    }
}
