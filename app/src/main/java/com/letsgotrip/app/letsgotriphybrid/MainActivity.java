package com.letsgotrip.app.letsgotriphybrid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private int	uiOption;

    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;
    public WebView webView;

    public String urlStr = "http://app.letsgotrip.com";
//    public static String urlStr = "http://192.168.0.100:8080/";
    private static final String target_url_prefix="app.letsgotrip.com";
    private WebView mWebviewPop;
    private FrameLayout mContainer;

    // 사용자 위치 수신기
    private LocationManager locationManager;
    private LocationListener locationListener;

    double latitude;
    double longitude;

    private WebViewInterface mWebViewInterface;
    private WebViewInterface mWebViewInterface2;

    public static Context mContext;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;

    WebView newView = null;


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

        //2018-02-06 00:3
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webSettings.setAppCacheEnabled(true);
        //

        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용을 허용한다.
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {
            //2018-02-06 00:3
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String host = Uri.parse(url).getHost();
                //Log.d("shouldOverrideUrlLoading", url);
                if (host.equals(target_url_prefix))
                {
                    // This is my web site, so do not override; let my WebView load
                    // the page
                    if(mWebviewPop!=null)
                    {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop=null;
                    }
                    return false;
                }

                if(host.equals("m.facebook.com")|| host.equals("www.facebook.com"))
                {
                    return false;
                }
                // Otherwise, the link is not for a page on my site, so launch
                // another Activity that handles URLs
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                Log.d("onReceivedSslError", "onReceivedSslError");
                //super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // First, get the URL that Facebook's login button is actually redirecting you to.
                // It should be something simulator to https://www.facebook.com/dialog/return/arbiter?relation=opener&close=true
                String webUrl = webView.getUrl();
                // Pass it to the LogCat so that you can then use it in the if statement below.

                if (url.startsWith(urlStr)) {
                    // Check whether the current URL is the URL that Facebook's redirecting you to.
                    // If it is - that's it - do what you want to after the logging process has finished.
                    return;
                }

                super.onPageFinished(view, url);
            }
            //
        });

        webView.setWebChromeClient(new WebChromeClient() {
            //2018-02-06 00:3
//            @Override
//            public void onCloseWindow(WebView window) {
//                super.onCloseWindow(window);
//                window.setVisibility(View.GONE);
//                webView.removeView(window);
//                newView = null;
//            }
//
//            @Override
//            public boolean onCreateWindow(WebView view, boolean isDialog,boolean isUserGesture,Message resultMsg) {
//                webView.removeAllViews();
//                newView = new WebView(view.getContext());
//                newView.setWebViewClient(new WebViewClient());
//
//                WebSettings settings = newView.getSettings();
//                settings.setJavaScriptEnabled(true);
//                newView.setWebChromeClient(this);
////                newView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//
//                newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//                mWebViewInterface2 = new WebViewInterface(MainActivity.this, newView); //JavascriptInterface 객체화
//                newView.addJavascriptInterface(mWebViewInterface2, "Android"); //웹뷰에 JavascriptInterface를 연결
//
//                webView.addView(newView);
//                decorView.setSystemUiVisibility( uiOption );
//
//                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
//                transport.setWebView(newView);
//                resultMsg.sendToTarget();
//                return true;
//            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                mWebviewPop = new WebView(view.getContext());
                mWebviewPop.setVerticalScrollBarEnabled(false);
                mWebviewPop.setHorizontalScrollBarEnabled(false);
                mWebviewPop.setWebViewClient(new WebViewClient() {
                    //2018-02-06 00:3
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        String host = Uri.parse(url).getHost();
                        //Log.d("shouldOverrideUrlLoading", url);
                        if (host.equals(target_url_prefix))
                        {
                            // This is my web site, so do not override; let my WebView load
                            // the page
                            if(mWebviewPop!=null)
                            {
                                mWebviewPop.setVisibility(View.GONE);
                                webView.removeView(mWebviewPop);
                                mWebviewPop=null;
                            }
                            return false;
                        }

                        if(host.equals("m.facebook.com"))
                        {
                            return false;
                        }
                        // Otherwise, the link is not for a page on my site, so launch
                        // another Activity that handles URLs
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                                   SslError error) {
                        Log.d("onReceivedSslError", "onReceivedSslError");
                        //super.onReceivedSslError(view, handler, error);
                    }
                    //
                });
                mWebviewPop.getSettings().setJavaScriptEnabled(true);
                mWebviewPop.getSettings().setSavePassword(false);
                mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                webView.addView(mWebviewPop);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(mWebviewPop);
                resultMsg.sendToTarget();

                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                Log.d("onCloseWindow", "called");
            }
        });

        mWebViewInterface = new WebViewInterface(MainActivity.this, webView); //JavascriptInterface 객체화
        webView.addJavascriptInterface(mWebViewInterface, "Android"); //웹뷰에 JavascriptInterface를 연결

        webView.loadUrl(urlStr+"/main3.do");


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
                if(currentLocation==null){
                    double lng = 106.707018;
                    double lat = 10.731659;
                }else{
                    double lng = currentLocation.getLongitude();
                    double lat = currentLocation.getLatitude();
                }
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

        @JavascriptInterface
        public String getToken () {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            return refreshedToken;
        }

        @JavascriptInterface
        public String getFacebook () {
//            FacebookSdk.sdkInitialize(getApplicationContext());
//            CallbackManager callbackManager = CallbackManager.Factory.create();
//            //LoginManager - 요청된 읽기 또는 게시 권한으로 로그인 절차를 시작합니다.
//            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,Arrays.asList("public_profile","email"));
//            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                        @Override
//                        public void onSuccess(LoginResult loginResult) { //로그인 성공시 호출되는 메소드
//                            //loginResult.getAccessToken() 정보를 가지고 유저 정보를 가져올수 있습니다.
//                            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken() ,
//                                    new GraphRequest.GraphJSONObjectCallback() {
//                                        @Override
//                                        public void onCompleted(JSONObject object, GraphResponse response) {
//                                            try {
//                                                Log.e("user profile",object.toString());
////                                                sendRequestSNS("facebookLoginCheck",object,"F");
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    });
//                            Bundle parameters = new Bundle();
//                            parameters.putString("fields", "id,name,email");
//                            request.setParameters(parameters);
//                            request.executeAsync();
//                        }
//
//                        @Override
//                        public void onCancel() {
//                            Log.e("onCancel", "onCancel");
//                        }
//
//                        @Override
//                        public void onError(FacebookException exception) {
//                            Log.e("onError", "onError " + exception.getLocalizedMessage());
//                        }
//                    });
            return null;
        }

        @JavascriptInterface
        public String getGoogle () {
//            @OnClick(R.id.google_signin_button)
//            void googleSignIn(){
//                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//                startActivityForResult(signInIntent,RC_SIGN_IN);
//            }
//
//            @Override
//            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//                super.onActivityResult(requestCode, resultCode, data);
//                callbackManager.onActivityResult(requestCode, resultCode, data);
//                if(requestCode==RC_SIGN_IN){
//                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//
//                    if(result.isSuccess()){
//                        GoogleSignInAccount account = result.getSignInAccount();
//                        firebaseAuthWithGoogle(account);
//                        JSONObject gresult = new JSONObject();
//                        try {
//                            gresult.put("email",result.getSignInAccount().getEmail());
//                            gresult.put("id",result.getSignInAccount().getId());
//                            gresult.put("name",result.getSignInAccount().getDisplayName());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        sendRequestSNS("facebookLoginCheck",gresult,"G");
//
////                Toast.makeText(this, "Login signed in success", Toast.LENGTH_LONG).show();
//                    }
//                }else{
////            Toast.makeText(LoginActivity.this, "Login signed in failed", Toast.LENGTH_LONG).show();
//                }
//            }
            return null;
        }


    }

    public void refesh(){
        this.finish();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        if(webView.canGoBack()){
            webView.goBack();
        }else if(newView!=null){
            webView.removeView(newView);
            newView = null;
        }
        else if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
