package com.letsgotrip.app.letsgotriphybrid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private int	uiOption;

    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;
    public WebView webView;

    public String urlStr = "http://app.letsgotrip.com/";
//    public static String urlStr = "http://192.168.0.101:8080/";

    // 사용자 위치 수신기
    private LocationManager locationManager;
    private LocationListener locationListener;

    double latitude;
    double longitude;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    private WebViewInterface mWebViewInterface;
    private WebViewInterface mWebViewInterface2;

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        settingGPS();

        // 사용자의 현재 위치 //
        Location userLocation = getMyLocation();

        if( userLocation != null ) {
            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
        }

        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();


        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
            uiOption |= SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            uiOption |= SYSTEM_UI_FLAG_FULLSCREEN;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(uiOption);
                        }
                    }
                });

        decorView.setSystemUiVisibility( uiOption );

        webView = (WebView)findViewById(R.id.mainWebView);

        webView.clearHistory();
        webView.clearCache(true);

        WebSettings webSettings = webView.getSettings();
        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용을 허용한다.
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {});

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                window.setVisibility(View.GONE);
                webView.removeView(window);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,boolean isUserGesture,Message resultMsg) {
                webView.removeAllViews();
                WebView newView = new WebView(view.getContext());
                newView.setWebViewClient(new WebViewClient());

                WebSettings settings = newView.getSettings();
                settings.setJavaScriptEnabled(true);
                newView.setWebChromeClient(this);
//                newView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

                newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                mWebViewInterface2 = new WebViewInterface(MainActivity.this, newView); //JavascriptInterface 객체화
                newView.addJavascriptInterface(mWebViewInterface2, "Android"); //웹뷰에 JavascriptInterface를 연결

                webView.addView(newView);
                decorView.setSystemUiVisibility( uiOption );

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        mWebViewInterface = new WebViewInterface(MainActivity.this, webView); //JavascriptInterface 객체화
        webView.addJavascriptInterface(mWebViewInterface, "Android"); //웹뷰에 JavascriptInterface를 연결

        webView.loadUrl(urlStr+"main3.do");


    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event){
//        if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }



    /**
     * 사용자의 위치를 수신
     */
    public Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2000);
        }
        else {
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }else{
                String networkProvider = LocationManager.NETWORK_PROVIDER;
                currentLocation = locationManager.getLastKnownLocation(networkProvider);
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
        }

        return currentLocation;
    }

    /**
     * GPS 를 받기 위한 매니저와 리스너 설정
     */
    public void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    /**
     * GPS 권한 응답에 따른 처리
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    boolean canReadLocation = false;
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == 2000) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
// success!
                Location userLocation = getMyLocation();
                if( userLocation != null ) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();
                }
                canReadLocation = true;
            } else {
// Permission was denied or request was cancelled
                canReadLocation = false;
            }
        }
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

    public void refesh(){
        this.finish();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
