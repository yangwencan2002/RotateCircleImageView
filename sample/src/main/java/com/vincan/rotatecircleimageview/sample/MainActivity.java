package com.vincan.rotatecircleimageview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.vincan.rotatecircleimageview.RotateCircleImageView;
import com.vincan.rotatecircleimageview.RotateCircleImageView.BorderStyle;

/**
 * @author vincanyang
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RotateCircleImageView rotateCircleImageView = (RotateCircleImageView) findViewById(R.id.rotateCircleImageView);
        rotateCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rotateCircleImageView.getBorderStyle()) {
                    case ROTATE:
                        rotateCircleImageView.setBorderStyle(BorderStyle.STILL);
                        rotateCircleImageView.setBorderColors(getResources().getIntArray(R.array.border_colors));
                        break;
                    case STILL:
                        rotateCircleImageView.setBorderStyle(BorderStyle.ROTATE);
                        break;
                }
            }
        });
    }
}
