package com.owwlo.courier.s.ui;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.owwlo.courier.R;
import com.owwlo.courier.s.Constants;
import com.owwlo.courier.s.CourierSService;
import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.CourierSService.ServiceBinder;
import com.owwlo.courier.s.data.Contact;
import com.owwlo.courier.s.poster.MessagePosterManager;
import com.owwlo.courier.s.utils.Utils;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "CourierSMainActivity";

    private Context mContext;
    private CourierSService mCourierService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(
                ComponentName paramAnonymousComponentName,
                IBinder paramAnonymousIBinder) {
            mCourierService = ((CourierSService.ServiceBinder) paramAnonymousIBinder)
                    .getService();
            mStateInfo.setText("I am frome Service :"
                    + mCourierService.getSystemTime());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private Button mStartServiceButton;
    private TextView mStateInfo;
    private Button mStopServiceButton;
    private ImageView mImage;

    private void initializeComponent() {
        mContext = this;
        mStateInfo = ((TextView) findViewById(R.id.state_text));
        mStartServiceButton = ((Button) findViewById(R.id.btn_start_service));
        mStopServiceButton = ((Button) findViewById(R.id.btn_stop_service));
        mStartServiceButton.setOnClickListener(this);
        mStopServiceButton.setOnClickListener(this);
        mImage = (ImageView) findViewById(R.id.image);

    }

    public void onClick(View paramView) {
        //Intent i = new Intent(this, PopupActivity.class);
        //this.startActivity(i);
        if (paramView == mStartServiceButton) {
            Intent localIntent1 = new Intent();
            localIntent1.setClass(this, CourierSService.class);
            mContext.startService(localIntent1);
            Log.i(TAG, "start button pressed.");
            Contact contact = Utils.getContactInfoFormPhone(mContext, "3475569640");
            mImage.setImageBitmap(contact.getImage(mContext));
        }
        if (paramView == mStopServiceButton) {
            Intent localIntent2 = new Intent();
            localIntent2.setClass(this, CourierSService.class);
            mContext.stopService(localIntent2);
            Log.i(TAG, "stop button pressed.");
        }
    }

    public void onCreate(Bundle paramBundle) {
        Log.i(TAG, "create.");
        super.onCreate(paramBundle);
//        setContentView(R.layout.activity_main);
//        initializeComponent();

        //bindService(new Intent(this, CourierSService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

        Intent s = new Intent(this, CourierSService.class);
        startService(s);

        Intent i = new Intent(this, PopupActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.unbindService(mServiceConnection);
    }

}
