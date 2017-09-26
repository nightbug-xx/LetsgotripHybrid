package com.letsgotrip.app.letsgotriphybrid;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class PopupActivity extends AppCompatActivity {
    private View decorView;
    private int	uiOption;

    private WebView webView;

    private String urlStr = "";
    private String atitle = "";

    double latitude;
    double longitude;

    private WebViewInterface mWebViewInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        urlStr = intent.getExtras().getString("url");
        atitle = intent.getExtras().getString("title");
//        this.s_type = intent.getExtras().getString("s_type");
//        this.s_name = intent.getExtras().getString("s_name");

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);			//액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);		//액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);			//홈 아이콘을 숨김처리합니다.

        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        View mCustomView = LayoutInflater.from(this).inflate(R.layout.layout_actionbar, null);
        actionBar.setCustomView(mCustomView);

        TextView v = (TextView)findViewById(R.id.evDetailTitle);
        v.setText(atitle);


        // 액션바에 백그라운드 색상을 아래처럼 입힐 수 있습니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,051,051,051)));

//        showNotiPannel = false;
        ImageButton btn = (ImageButton) findViewById(R.id.detailNavi);

        btn.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(EventDetailActivity.this, DetailNaviActivity.class);
//                        intent.putExtra("s_name",s_name);
//                        intent.putExtra("sidx",sidx);
//                        intent.putExtra("cidx",cidx);
//                        intent.putExtra("s_type",s_type);
//                        startActivity(intent);
                    }
                }
        );

        ImageButton btn2 = (ImageButton) findViewById(R.id.customCancel);

        btn2.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);




        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
            uiOption |= SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            uiOption |= SYSTEM_UI_FLAG_FULLSCREEN;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility( uiOption );

        webView = (WebView)findViewById(R.id.popupWebView);

        webView.clearHistory();
        webView.clearCache(true);

        WebSettings webSettings = webView.getSettings();
        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용을 허용한다.
        webView.setWebViewClient(new WebViewClient());  // 새로운 창을 띄우지 않고 내부에서 웹뷰를 실행시킨다.
        webSettings.setSupportMultipleWindows(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
//                Toast.makeText(PopupActivity.this, "노딩중 : "+String.valueOf(progress), Toast.LENGTH_SHORT).show();
//                super.onProgressChanged(view, progress);
//                activity.setProgress(progress * 1000);
            }

//            @Override
//            public boolean onCreateWindow(WebView view, boolean isDialog,boolean isUserGesture,Message resultMsg) {
//                WebView.HitTestResult result = view.getHitTestResult();
//                String url = result.getExtra();
//
//                Intent intent=new Intent(MainActivity.this,PopupActivity.class);
//                if(url.contains("/member/login.do")){;
//                    intent.putExtra("navi",false);
//                    intent.putExtra("title","로그인");
//                }
//
//                intent.putExtra("url",url);
//                startActivity(intent);
//
//                return  false;
//            }
        });


        mWebViewInterface = new WebViewInterface(PopupActivity.this, webView); //JavascriptInterface 객체화
        webView.addJavascriptInterface(mWebViewInterface, "Android"); //웹뷰에 JavascriptInterface를 연결

        webView.loadUrl(urlStr);


    }



    public class WebViewInterface  {

        private WebView mAppView;
        private Activity mContext;
        private Location location;

        //나의 위도 경도 고도
        double mLatitude;  //위도
        double mLongitude; //경도

        protected LocationManager locationManager;

        /**
         * 생성자.
         * @param activity : context
         * @param view : 적용될 웹뷰
         */
        public WebViewInterface(Activity activity, WebView view) {
            mAppView = view;
            mContext = activity;
        }

        public WebViewInterface(Activity activity, WebView view, Location myLocation) {
            mAppView = view;
            mContext = activity;
            location = myLocation;
        }

        /**
         * 안드로이드 토스트를 출력한다. Time Long.
         * @param message : 메시지
         */
        @JavascriptInterface
        public void toastLong (String message) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
        /**
         * 안드로이드 토스트를 출력한다. Time Short.
         * @param message : 메시지
         */
        @JavascriptInterface
        public void toastShort (String message) { // Show toast for a short time
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * 현재 GPS정보를 가져온다.
         */
        @JavascriptInterface
        public String getLocation () { // Show toast for a short time
            return "{\"lat\":"+String.valueOf(latitude)+",\"lng\":"+String.valueOf(longitude)+"}";
        }


    }

}
