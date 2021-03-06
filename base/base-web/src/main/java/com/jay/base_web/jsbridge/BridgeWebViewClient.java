package com.jay.base_web.jsbridge;

import android.graphics.Bitmap;
import android.os.Build;

import com.jay.base_lib.utils.L;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/** 如果要自定义WebViewClient必须要集成此类 Created by bruce on 10/28/15. */
public class BridgeWebViewClient extends WebViewClient {

  private BossBridgeWebView webView;
  private WebViewClientListener listener;

  public BridgeWebViewClient(BossBridgeWebView webView) {
    this.webView = webView;
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (listener != null) {
      listener.shouldOverrideUrlLoading(view, url);
    }
    try {
      L.d(BossBridgeWebView.TAG, "BridgeWebViewClient-url=" + url);
      url = URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
      webView.handlerReturnData(url);
      return true;
    } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
      webView.flushMessageQueue();
      return true;
    } else {
      return this.onCustomShouldOverrideUrlLoading(url)
          ? true
          : super.shouldOverrideUrlLoading(view, url);
    }
  }

  // 增加shouldOverrideUrlLoading在api》=24时
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      String url = request.getUrl().toString();
      try {
        url = URLDecoder.decode(url, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      }
      if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
        webView.handlerReturnData(url);
        return true;
      } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
        webView.flushMessageQueue();
        return true;
      } else {
        return this.onCustomShouldOverrideUrlLoading(url)
            ? true
            : super.shouldOverrideUrlLoading(view, request);
      }
    } else {
      return super.shouldOverrideUrlLoading(view, request);
    }
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    super.onPageStarted(view, url, favicon);
    if (listener != null) {
      listener.onPageStarted(view, url, favicon);
    }
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    if (listener != null) {
      listener.onPageFinished(view, url);
    }
    if (BossBridgeWebView.toLoadJs != null) {
      BridgeUtil.webViewLoadLocalJs(view, BossBridgeWebView.toLoadJs);
    }

    //
    if (webView.getStartupMessage() != null) {
      for (Message m : webView.getStartupMessage()) {
        webView.dispatchMessage(m);
      }
      webView.setStartupMessage(null);
    }

    //
    onCustomPageFinishd(view, url);
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    super.onReceivedError(webView, errorCode, description, failingUrl);
    if (listener != null) {
      listener.onReceivedError(webView, errorCode, description, failingUrl);
    }
  }

  protected boolean onCustomShouldOverrideUrlLoading(String url) {
    return false;
  }

  protected void onCustomPageFinishd(WebView view, String url) {}

  public void setWebViewClientListener(WebViewClientListener listener) {
    this.listener = listener;
  }
}
