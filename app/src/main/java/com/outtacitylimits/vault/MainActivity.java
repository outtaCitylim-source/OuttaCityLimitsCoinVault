package com.outtacitylimits.vault;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** The web inventory service owns authentication, records, and upload authorization. */
public class MainActivity extends Activity {
    private static final String ORIGIN = "https://outta-city-limits.vercel.app";
    private static final int OCL_GREEN = 0xFF173F35;
    private static final int OCL_GOLD = 0xFFE2B75A;
    private static final int PHOTO_REQUEST = 100;
    private WebView web;
    private TextView error;
    private ValueCallback<Uri[]> photoCallback;
    private Uri cameraUri;
    private File cameraFile;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF5F2EA);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(24, 16, 24, 14);
        brand.setBackgroundColor(OCL_GREEN);
        TextView brandName = new TextView(this);
        brandName.setText("OUTTA CITY LIMITS");
        brandName.setTextColor(OCL_GOLD);
        brandName.setTextSize(13);
        brandName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brandName.setLetterSpacing(0.12f);
        brand.addView(brandName);
        TextView appName = new TextView(this);
        appName.setText("Coin Vault");
        appName.setTextColor(0xFFFFFFFF);
        appName.setTextSize(23);
        appName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.addView(appName);
        TextView tagline = new TextView(this);
        tagline.setText("Post, Texas  ·  Collect beyond the obvious");
        tagline.setTextColor(0xFFD8E1DB);
        tagline.setTextSize(12);
        brand.addView(tagline);
        root.addView(brand);
        LinearLayout navigation = new LinearLayout(this);
        navigation.setPadding(8, 2, 8, 2);
        navigation.setBackgroundColor(0xFFECE8DE);
        addNavigation(navigation, "Shop", "/shop");
        addNavigation(navigation, "Inventory", "/admin/products");
        addNavigation(navigation, "Account", "/account");
        Button refresh = new Button(this);
        refresh.setText("Reload");
        refresh.setTextColor(OCL_GREEN);
        refresh.setBackgroundTintList(ColorStateList.valueOf(0xFFECE8DE));
        refresh.setOnClickListener(view -> web.reload());
        navigation.addView(refresh, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(navigation);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        root.addView(progress, new LinearLayout.LayoutParams(-1, 6));
        error = new TextView(this);
        error.setPadding(24, 16, 24, 16);
        error.setVisibility(View.GONE);
        root.addView(error);
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(true);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isOcl(uri)) return false;
                if (!request.isForMainFrame()) return true;
                if ("https".equals(uri.getScheme()) || "mailto".equals(uri.getScheme()) || "tel".equals(uri.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                    catch (ActivityNotFoundException exception) { showError("No app is available to open this link."); }
                }
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                error.setVisibility(View.GONE);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError failure) {
                if (request.isForMainFrame()) showError("Unable to reach OCL. Check your connection, then tap Reload.");
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value == 100 ? View.GONE : View.VISIBLE);
            }
            @Override public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
                new AlertDialog.Builder(MainActivity.this).setMessage(message)
                    .setPositiveButton("Continue", (dialog, which) -> result.confirm())
                    .setNegativeButton("Cancel", (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel()).show();
                return true;
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (!isOcl(Uri.parse(view.getUrl() == null ? "" : view.getUrl()))) return false;
                finishPhotos(null);
                photoCallback = callback;
                new AlertDialog.Builder(MainActivity.this).setTitle("Coin photos")
                    .setItems(new String[]{"Take a photo", "Choose existing photos"}, (dialog, which) -> {
                        if (which == 0) takePhoto(); else choosePhotos(params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                    }).setOnCancelListener(dialog -> finishPhotos(null)).show();
                return true;
            }
        });
        // TEMPORARY: Create Native RecyclerView for the "Inventory" view
        androidx.recyclerview.widget.RecyclerView coinList = new androidx.recyclerview.widget.RecyclerView(this);
        coinList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        coinList.setBackgroundColor(0xFFF5F2EA);
        CoinAdapter adapter = new CoinAdapter();
        coinList.setAdapter(adapter);

        // Hide web view and show native list when hitting "Inventory"
        navigation.getChildAt(1).setOnClickListener(view -> {
            web.setVisibility(View.GONE);
            coinList.setVisibility(View.VISIBLE);
            
            // Generate some MOCK coins to show on screen
            ArrayList<Product> mockCoins = new ArrayList<>();
            
            Product p1 = new Product();
            p1.setTitle("1921 Morgan Silver Dollar");
            p1.setDescription("Beautiful uncirculated condition from the Philadelphia mint.");
            p1.setPrice(45.00);
            p1.setStatus("available");
            
            Product p2 = new Product();
            p2.setTitle("1943 Steel Penny");
            p2.setDescription("Rare wartime steel cent. Highly sought after.");
            p2.setPrice(12.50);
            p2.setStatus("sold");

            Product p3 = new Product();
            p3.setTitle("1909-S VDB Lincoln Cent");
            p3.setDescription("The holy grail of Lincoln cents. Graded VF-20.");
            p3.setPrice(1250.00);
            p3.setStatus("available");
            
            mockCoins.add(p1);
            mockCoins.add(p2);
            mockCoins.add(p3);
            
            adapter.setCoins(mockCoins);
        });

        // FrameLayout to hold both web and native list, so we can swap them
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        container.addView(web, new android.widget.FrameLayout.LayoutParams(-1, -1));
        coinList.setVisibility(View.GONE); // Hide initially
        container.addView(coinList, new android.widget.FrameLayout.LayoutParams(-1, -1));

        root.addView(container, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        if (state == null || web.restoreState(state) == null) web.loadUrl(ORIGIN + "/admin/products");
        root.requestApplyInsets();
    }

    private boolean isOcl(Uri uri) {
        return "https".equals(uri.getScheme()) && "outta-city-limits.vercel.app".equals(uri.getHost())
            && (uri.getPort() == -1 || uri.getPort() == 443);
    }

    private void addNavigation(LinearLayout bar, String label, String path) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(OCL_GREEN);
        button.setBackgroundTintList(ColorStateList.valueOf(0xFFECE8DE));
        button.setOnClickListener(view -> web.loadUrl(ORIGIN + path));
        bar.addView(button, new LinearLayout.LayoutParams(0, -2, 1));
    }

    private void showError(String message) { error.setText(message); error.setVisibility(View.VISIBLE); }

    private void choosePhotos(boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
        try { startActivityForResult(intent, PHOTO_REQUEST); }
        catch (ActivityNotFoundException exception) { finishPhotos(null); showError("A photo picker is unavailable."); }
    }

    private void takePhoto() {
        try {
            File directory = new File(getCacheDir(), "photos");
            if (!directory.exists() && !directory.mkdirs()) throw new IOException("Photo directory unavailable");
            cameraFile = File.createTempFile("coin-", ".jpg", directory);
            cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".photos", cameraFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            intent.setClipData(ClipData.newRawUri("Coin photo", cameraUri));
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PHOTO_REQUEST);
        } catch (IOException | ActivityNotFoundException exception) {
            finishPhotos(null);
            showError("Camera unavailable. Choose an existing photo instead.");
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PHOTO_REQUEST) return;
        ArrayList<Uri> selected = new ArrayList<>();
        if (result == RESULT_OK) {
            if (cameraUri != null && cameraFile != null && cameraFile.length() > 0) selected.add(cameraUri);
            else if (data != null && data.getClipData() != null) {
                ClipData clip = data.getClipData();
                for (int i = 0; i < Math.min(clip.getItemCount(), 12); i++) selected.add(clip.getItemAt(i).getUri());
            } else if (data != null && data.getData() != null) selected.add(data.getData());
        }
        // Only content URIs from the system picker or our camera provider reach the web upload input.
        selected.removeIf(uri -> !"content".equals(uri.getScheme()));
        finishPhotos(selected.isEmpty() ? null : selected.toArray(new Uri[0]));
    }

    private void finishPhotos(Uri[] photos) {
        if (photoCallback != null) photoCallback.onReceiveValue(photos);
        photoCallback = null;
        cameraUri = null;
        cameraFile = null;
    }

    @Override protected void onSaveInstanceState(Bundle state) { web.saveState(state); super.onSaveInstanceState(state); }
    @Override protected void onPause() { CookieManager.getInstance().flush(); super.onPause(); }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { finishPhotos(null); web.destroy(); super.onDestroy(); }
}
