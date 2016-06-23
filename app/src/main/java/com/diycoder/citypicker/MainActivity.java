package com.diycoder.citypicker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diycoder.citypick.widget.CityPickerPopWindow;

public class MainActivity extends AppCompatActivity implements CityPickerPopWindow.CityPickListener {

    private Activity mContext;
    private LinearLayout rootView;
    private TextView cityView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        rootView = (LinearLayout) findViewById(R.id.root_layout);
        cityView = (TextView) findViewById(R.id.city);
    }

    public void selectCity(View view) {
        CityPickerPopWindow mPopWindow = new CityPickerPopWindow(mContext);
        mPopWindow.showPopupWindow(rootView);
        mPopWindow.setCityPickListener(this);
    }

    @Override
    public void pickValue(String value) {
        cityView.setText(value);
    }
}
