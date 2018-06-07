package com.example.loong.mylocation;

/**
 * Created by Loong on 2017/7/4. 13:21
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PaycodeActivity extends Activity {
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_code);
        layout = (LinearLayout) findViewById(R.id.payCodeDialog);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "科技以改善生活为本",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        finish();
        overridePendingTransition(R.transition.in, R.transition.exit);
        return true;
    }

}