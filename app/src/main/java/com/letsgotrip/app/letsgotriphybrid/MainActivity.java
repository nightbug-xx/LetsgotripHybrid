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
import android.support.annotation.NonNull;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private int	uiOption;

    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;
    public WebView webView;

    public String urlStr = "http://app.letsgotrip.com";
//    public static String urlStr = "http://192.168.0.100:8080";
    private static final String target_url_prefix="app.letsgotrip.com";
//    private static final String target_url_prefix="192.168.0.100:8080";
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

    int RC_SIGN_IN = 666;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        settingGPS();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


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
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        //

        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용을 허용한다.
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {
            //2018-02-06 00:3
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String host = Uri.parse(url).getHost();
                if (host.equals(target_url_prefix))
                {
                    // This is my web site, so do not override; let my WebView load
                    // the page
                    if(newView!=null)
                    {
                        newView.setVisibility(View.GONE);
                        mContainer.removeView(newView);
                        newView=null;
                    }
                    return false;
                }

//                if(host.equals("m.facebook.com")|| host.equals("www.facebook.com")||host.contains("firebase")||host.contains("google")||host.contains("blank"))
//                if(!host.startsWith("app.letsgotrip.com"))
//                {
//                    return false;
//                }
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
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                window.setVisibility(View.GONE);
                webView.removeView(window);
                newView = null;
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,boolean isUserGesture,Message resultMsg) {
                webView.removeAllViews();
                newView = new WebView(view.getContext());
                newView.setWebViewClient(new WebViewClient());

                WebSettings settings = newView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setSupportMultipleWindows(true);

                newView.setWebChromeClient(this);

                newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                mWebViewInterface2 = new WebViewInterface(MainActivity.this, newView); //JavascriptInterface 객체화
                newView.addJavascriptInterface(mWebViewInterface2, "Android2"); //웹뷰에 JavascriptInterface를 연결

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

        webView.loadUrl(urlStr+"/main3.do");


    }


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
        public String getLocation () {
            return "{\"lat\":"+String.valueOf(latitude)+",\"lng\":"+String.valueOf(longitude)+"}";
        }

        @JavascriptInterface
        public String getToken () {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            return refreshedToken;
        }

        @JavascriptInterface
        public String getGoogle () {
            signIn();
            return null;
        }


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("11111111111","111111111111");
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("tag", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("tag", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            webView.loadUrl(urlStr+"/member/ggLogin.do?pass="+user.getUid()+"&fname="+user.getDisplayName()+"&lname=&email="+user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("tag", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//        if(requestCode==RC_SIGN_IN){
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//
//            if(result.isSuccess()){
//                GoogleSignInAccount account = result.getSignInAccount();
//                firebaseAuthWithGoogle(account);
//                JSONObject gresult = new JSONObject();
//                try {
//                    gresult.put("email",result.getSignInAccount().getEmail());
//                    gresult.put("id",result.getSignInAccount().getId());
//                    gresult.put("name",result.getSignInAccount().getDisplayName());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                sendRequestSNS("facebookLoginCheck",gresult,"G");
//
////                Toast.makeText(this, "Login signed in success", Toast.LENGTH_LONG).show();
//            }
//        }else{
////            Toast.makeText(LoginActivity.this, "Login signed in failed", Toast.LENGTH_LONG).show();
//        }
//    }

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
