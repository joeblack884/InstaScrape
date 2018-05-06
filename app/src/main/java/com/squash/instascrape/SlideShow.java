package com.squash.instascrape;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class SlideShow extends Activity {
    String heartURLStr = "";

    class HtmlHandler {
        Handler handler;
        public HtmlHandler(Handler h) {
            handler = h;
        }
        @JavascriptInterface
        public void handleHtml(String jsonPosts, String maxDate) {

            final ImageView logoView = (ImageView) findViewById(R.id.logo);
            final TextView nameView = (TextView) findViewById(R.id.name);
            final TextView dateView = (TextView) findViewById(R.id.time);
            final TextView infoView = (TextView) findViewById(R.id.showInfo);
            final ImageView imageView = (ImageView) findViewById(R.id.imageview);
            final VideoView videoView = (VideoView) findViewById(R.id.videoview);
            final ImageView heartView = (ImageView) findViewById(R.id.heart);
            final TextView likesView = (TextView) findViewById(R.id.likes);
            final TextView descriptionView = (TextView) findViewById(R.id.description);

            try {
                JSONObject posts = new JSONObject(jsonPosts);

                Iterator<String> k = posts.keys();
                while (k.hasNext()) {
                    String imageURL = k.next();
                    JSONObject post = posts.getJSONObject(imageURL);
                    final String showInfo = post.getString("postNum") + "/" + posts.length() + " " + maxDate;
                    String logoURL = post.getString("logoURL");
                    final String username = post.getString("username");
                    String datetime = post.getString("datetime");
                    heartURLStr = post.getString("heartURLStr");

                    String tempLikes = "";
                    String tempDesc = "";
                    String tempVideoURL = "";
                    if (post.has("numLikes")) {
                        tempLikes = post.getString("numLikes");
                    }
                    if (post.has("descStr")) {
                        tempDesc = post.getString("descStr");
                    }
                    if (post.has("videoURL")) {
                        tempVideoURL = post.getString("videoURL");
                    }
                    final String likes = tempLikes;
                    final String desc = tempDesc;
                    final String videoURL = tempVideoURL;

                    try {
                        final String dateString = datetime;
                        URL lURL = new URL(logoURL);
                        URLConnection urlConnection = lURL.openConnection();
                        InputStream iStream = urlConnection.getInputStream();
                        final Bitmap lBMP = BitmapFactory.decodeStream(iStream);
                        Bitmap tempBMP = null;
                        if (tempVideoURL.isEmpty()) {
                            URL url = new URL(imageURL);
                            tempBMP = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        }
                        final Bitmap bmp = tempBMP;
                        URL heartURL = new URL(heartURLStr);
                        final Bitmap heartBMP = BitmapFactory.decodeStream(heartURL.openConnection().getInputStream());

                        //handler.post(new Runnable() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    logoView.setImageBitmap(lBMP);
                                    nameView.setText(username);
                                    infoView.setText(showInfo);
                                    dateView.setText(dateString);
                                    if (videoURL.isEmpty()) {
                                        if (videoView.isPlaying()) {
                                            videoView.stopPlayback();
                                        }
                                        videoView.setVisibility(View.INVISIBLE);
                                        imageView.setVisibility(View.VISIBLE);
                                        imageView.setImageBitmap(bmp);
                                    } else {
                                        videoView.setVisibility(View.VISIBLE);
                                        imageView.setVisibility(View.INVISIBLE);
                                        videoView.setVideoPath(videoURL);

                                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                videoView.start();
                                            }
                                        });
                                        videoView.start();
                                    }
                                    heartView.setImageBitmap(heartBMP);
                                    likesView.setText(likes);
                                    descriptionView.setText(desc);
                                } catch (Exception e) {
                                    System.out.println(e.toString());
                                }
                            }
                        });

                        //Thread.sleep(300000, 0);
                        Thread.sleep(5000, 0);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    if (!k.hasNext()) {
                        k = posts.keys();
                    }
                }

            } catch (JSONException jsonEx) {
                System.out.println(jsonEx.toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        ImageView heartButton = (ImageView) findViewById(R.id.heart);
        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (heartURLStr.contains("Transparent")) {
                    heartURLStr = "http://www.pngall.com/wp-content/uploads/2016/04/Instagram-Heart-Free-Download-PNG.png";
                } else {
                    heartURLStr = "http://www.pngall.com/wp-content/uploads/2016/04/Instagram-Heart-Transparent.png";
                }
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        //webView.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);
        Handler handler = new Handler();
        webView.addJavascriptInterface(new HtmlHandler(handler), "HtmlHandler");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:" +
                    "var posts = {};" +
                    "var maxDate = '';" +
                    "var observer = new window.MutationObserver(function(mutations) { mutations.forEach(function(mutation) { if (mutation.type == 'attributes') {" +
                        "console.log('Muations'); " +
                        "console.log(mutation.target.getAttribute('src')); " +
                    "} }); });" +
                    "function readPosts() {" +
                        "console.log('reading...');" +

                        "var aElements = document.getElementsByTagName('a');" +
                        "for (var j = 0; j < aElements.length; j++) {" +
                            "if (aElements[j].text == '\u00a0more') {" +
                                "aElements[j].click();" +
                            "}" +
                        "}" +

                        "var postElems = document.getElementsByTagName('article');" +
                        "console.log('read ' + postElems.length + ' elements');" +
                        "for (var i = 0; i < postElems.length; i++) {" +
                            "var p = postElems[i];" +
                            "var d = new Date(p.getElementsByTagName('time')[0].getAttribute('datetime'));" +
                            "if (maxDate == '' || maxDate < d) {" +
                                "maxDate = d;" +
                            "}" +
                            "var postChildren = p.children;" +
                            "var header = postChildren[0];" +
                            "var imageDiv = postChildren[1];" +
                            "var descriptionDiv = postChildren[2];" +

                            //Get image and video
                            "var imageURL = '';" +
                            "var otherImageURLs = [];" +
                            "if (imageDiv.getElementsByTagName('img').length > 0) {" +
                                "imageURL = imageDiv.getElementsByTagName('img')[0].getAttribute('src');" +
                                "if (imageDiv.getElementsByTagName('a').length > 0) {" +
                                "console.log('has others: ' + imageDiv.getElementsByTagName('a')[0].classList);" +
                                    "var img = imageDiv.getElementsByTagName('img')[0];" +
                                    "var imgArrow = imageDiv.getElementsByTagName('a')[0];" +
                                    //"observer.observe(img, { attributes: true });" +
                                    "var altText = img.getAttribute('alt');" +
                                    "while (imgArrow.classList.contains('coreSpriteRightChevron')) {" +
                                        "imgArrow.click();" +
                                        "for (var x = 0; x < 1000000000; x++) { x+=2; }" +
                                        "img = document.querySelector('[alt=\"' + altText + '\"]');" +
                                        "imgArrow = img.parentElement.parentElement.parentElement.parentElement.children[1];" +
                                "console.log('other: ' + img.attributes.length);" +
                                "console.log('other: ' + img.attributes.item(0).name);" +
                                "console.log('other: ' + img.attributes.item(1).name);" +
                                "console.log('other: ' + img.attributes.item(2).name);" +
                                "console.log(img);" +
                                        "otherImageURLs.push(img.getAttribute('src'));" +
                                    "}" +
                                "}" +
                            "}" +
                            "var videoURL = '';" +
                            "if (imageDiv.getElementsByTagName('video').length > 0) {" +
                                "videoURL = imageDiv.getElementsByTagName('video')[0].getAttribute('src');" +
                            "}" +

                            //Get user's name
                            "var nameElements = header.children[1].getElementsByTagName('a');" +
                            "var username = '';" +
                            "if (nameElements.length == 1) {" +
                                // Username only
                                "username = nameElements[0].text;" +
                            "}" +
                            "else if (nameElements.length > 1) {" +
                                // Username & Location
                                "username = nameElements[0].text + '\\n' + nameElements[1].text;" +
                            "}" +

                            // Get heart state
                            "var classes = descriptionDiv.children[0].getElementsByTagName('span')[0].className;" +
                            "var heartURLStr = '';" +
                            "if (classes.includes('coreSpriteHeartOpen')) {" +
                                "heartURLStr = 'http://www.pngall.com/wp-content/uploads/2016/04/Instagram-Heart-Transparent.png';" +
                            "}" +
                            "else if (classes.includes('coreSpriteHeartFull')) {" +
                                "heartURLStr = 'http://www.pngall.com/wp-content/uploads/2016/04/Instagram-Heart-Free-Download-PNG.png';" +
                            "}" +

                            // Get number of likes
                            "var numLikes = '0';" +
                            // a tags appear for images
                            "var aTags = descriptionDiv.children[1].getElementsByTagName('a');" +
                            "var aTagsSize = aTags.length;" +

                            "if (aTagsSize == 1) {" +
                                // One a tag means # of likes
                                "numLikes = aTags[0].text;" +
                            "}" +
                            "else if (aTagsSize > 1) {" +
                                // Many a tags means list of users that like it
                                "numLikes = aTagsSize.toString() + ' likes';" +
                            "}" +
                            "else {" +
                                // span tags appear for videos
                                "var spanTags = descriptionDiv.children[1].getElementsByTagName('span');" +
                                "if (spanTags.length > 0) {" +
                                    "numLikes = spanTags[0].text;" +
                                "}" +
                            "}" +

                            "if (!(imageURL in posts)) {" +
                                "posts[imageURL] = {" +
                                    "postNum: Object.keys(posts).length + 1," +
                                    "otherImageURLs: otherImageURLs," +
                                    "videoURL: videoURL," +
                                    "logoURL: header.getElementsByTagName('img')[0].getAttribute('src')," +
                                    "username: username," +
                                    "datetime: d.toLocaleDateString() + ' ' + d.toLocaleTimeString('en-US')," +
                                    "heartURLStr: heartURLStr," +
                                    "numLikes: numLikes," +
                                    "descStr: descriptionDiv.getElementsByTagName('ul')[0].getElementsByTagName('span')[0].textContent" +
                                "};" +
                                "console.log('new post found! ' + Object.keys(posts).length + ' total');" +
                            "}" +
                        "}" +
                    "}" +

                    "readPosts();" +
                    //"var numReads = 20;" +
                    "var numReads = 2;" +
                    "var mSecBetweenReads = 3000;" +
                    "for (var i = 0; i < numReads; i++) {" +
                        "setTimeout( function() { window.scrollBy(0,5000); readPosts();}, (i+1)*mSecBetweenReads);" +
                    "}" +
                    "setTimeout( function() { " +
                        "readPosts();" +
                        "HtmlHandler.handleHtml(" +
                                "JSON.stringify(posts), " +
                                "('(' + maxDate.toDateString() + ' ' + maxDate.toLocaleTimeString('en-US') + ')')" +
                        ");" +
                    "}, (numReads+1)*mSecBetweenReads);"
                );
                view.setVisibility(View.GONE);
            }
        });
        webView.loadUrl("https://www.instagram.com");
    }
}
