package com.owwlo.courier.s.ui;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.owwlo.courier.R;
import com.owwlo.courier.s.CourierSService;
import com.owwlo.courier.s.CourierSService.AuthcodeListener;
import com.owwlo.courier.s.poster.MessagePosterManager;
import com.owwlo.courier.s.utils.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

public class PopupActivity extends SherlockFragmentActivity {
    private static final String TAG = PopupActivity.class.getSimpleName();

    private CourierSService mCourierService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(
                ComponentName paramAnonymousComponentName,
                IBinder paramAnonymousIBinder) {
            Log.i(TAG, "binder");
            mCourierService = ((CourierSService.ServiceBinder) paramAnonymousIBinder)
                    .getService();

            onBindSuccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        bindService(new Intent(this, com.owwlo.courier.s.CourierSService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "destroy");
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private void onBindSuccess() {
        Log.i(TAG, "show dialog");
        ReadyDialog dialog = new ReadyDialog(this);
        dialog.show(this.getSupportFragmentManager(), null);
    }

    private class ReadyDialog extends SherlockDialogFragment {
        private Context mContext;

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setCancelable(true);
            this.setStyle(R.style.DialogStyle, R.style.TransparentTheme);
            return (new DialogContent(mContext));
        }

        public ReadyDialog(Context context) {
            mContext = context;
        }

        class DialogContent extends AlertDialog {

            /**
             * ReadyDialog 部分
             */
            private TextView mAuthcode;
            private TextView mAuthcodeBack;
            private ImageButton mRefreshButton;

             /**
              * StateDialog 部分
              */
            private Button mDisconnectBtn;
            private CheckBox mAutoConnect;

            private AuthcodeListener mAuthcodeListener = new AuthcodeListener() {
                @Override
                public void onAuthcodeChanged(
                        String newAuthcode) {
                    mAuthcode.setText(newAuthcode);
                }
            };

            public DialogContent(Context context) {
                super(context);
            }

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                prepareView();
                getWindow().setGravity(Gravity.CENTER);
            }

            private void prepareView() {
                MessagePosterManager mMessagePosterManager = MessagePosterManager
                        .getInstance();
                if (Utils.isLocalNetConnected(mContext)) {
                    if (!mMessagePosterManager.isConnectedToHost()) {
                        // 非连接的状态下

                        String authcode = mCourierService.getAuthCode();
                        Log.i(TAG, authcode);
                        setContentView(R.layout.ready_dialog);
                        getWindow().setLayout(500, 400);
                        initForReadyDialog();

                        // 加载数位字体
                        Typeface type = Typeface.createFromAsset(getAssets(),
                                "fonts/DS-DIGIT.TTF");
                        mAuthcode.setTypeface(type);
                        mAuthcodeBack.setTypeface(type);
                        mAuthcode.setText(authcode);

                        mCourierService.addAuthcodeListener(mAuthcodeListener);

                        mRefreshButton
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mCourierService.generateAuthCode();
                                    }
                                });
                    } else {
                        // 连接的状态下

                        setContentView(R.layout.connected_state_dialog);
                        getWindow().setLayout(500, 400);
                        initForStateDialog();

                        mAutoConnect.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                if (mAutoConnect.isChecked()) {

                                } else {

                                }
                            }
                        });

                        mDisconnectBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        });
                    }
                }
            }

            private void initForReadyDialog() {
                mAuthcode = (TextView) findViewById(R.id.authcodeText);
                mAuthcodeBack = (TextView) findViewById(R.id.authcodeTextBack);
                mRefreshButton = (ImageButton) findViewById(R.id.refreshButton);
            }

            private void initForStateDialog() {
                mDisconnectBtn = (Button) findViewById(R.id.disconnectButton);
                mAutoConnect = (CheckBox) findViewById(R.id.autoConnect);
            }

            @Override
            public void onDetachedFromWindow() {
                mCourierService.removeAuthcodeListener(mAuthcodeListener);
                PopupActivity.this.finish();
            }
        }
    }
}
