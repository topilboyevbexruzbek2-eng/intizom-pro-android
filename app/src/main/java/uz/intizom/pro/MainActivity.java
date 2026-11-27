package uz.intizom.pro;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.*;
import java.util.Locale;

/** Offline-first WebView shell for bundled 7.2 assets. */
public class MainActivity extends Activity {
 private static final int PICK_PDF=41,PICK_MEDIA=42,REQUEST_NOTIFICATIONS=43;
 private WebView web;
 private android.webkit.ValueCallback<Uri[]> chooser;
 private int currentPdfSlot=-1,currentMediaSlot=-1;
 private final String URL="file:///android_asset/index.html";
 @Override public void onCreate(Bundle state){super.onCreate(state);
  getWindow().setStatusBarColor(0xff101722);getWindow().setNavigationBarColor(0xff101722);
  web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(true);web.getSettings().setAllowFileAccessFromFileURLs(false);web.getSettings().setAllowUniversalAccessFromFileURLs(false);
  web.getSettings().setAllowContentAccess(true);web.setBackgroundColor(0xff101722);
  web.addJavascriptInterface(new Bridge(),"ReminderNative");
  web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){return !URL.equals(request.getUrl().toString());}});
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,android.webkit.ValueCallback<Uri[]> cb,FileChooserParams p){if(chooser!=null)chooser.onReceiveValue(null);chooser=cb;try{startActivityForResult(p.createIntent(),49);return true;}catch(Exception ex){chooser=null;return false;}}});
  setContentView(web);web.loadUrl(URL);ReminderScheduler.restore(this);
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQUEST_NOTIFICATIONS);
 }
 private File pdfFile(int slot){File dir=new File(getFilesDir(),"pdf");dir.mkdirs();return new File(dir,"book-"+slot+".pdf");}
 private File mediaFile(int slot){File dir=new File(getFilesDir(),"media");dir.mkdirs();boolean a=(slot>=32&&slot<40)||(slot>=149&&slot<249);return new File(dir,(a?"audio-":"video-")+slot+(a?".mp3":".mp4"));}
 private void copyInto(Uri u,File file,long limit)throws IOException {try(InputStream in=getContentResolver().openInputStream(u);OutputStream out=new FileOutputStream(file)){if(in==null)throw new IOException("No input");byte[] b=new byte[16384];int n;long total=0;while((n=in.read(b))!=-1){total+=n;if(total>limit)throw new IOException("File too big");out.write(b,0,n);}}catch(IOException e){file.delete();throw e;}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);
  if(request==49&&chooser!=null){chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));chooser=null;return;}
  if(result!=RESULT_OK||data==null||data.getData()==null)return;
  try{if(request==PICK_PDF&&currentPdfSlot>=0){copyInto(data.getData(),pdfFile(currentPdfSlot),100000000);}
    if(request==PICK_MEDIA&&currentMediaSlot>=0){copyInto(data.getData(),mediaFile(currentMediaSlot),150000000);}
  }catch(Exception ignored){}
 }
 private void pick(String mime,int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);startActivityForResult(i,request);}
 private String pageJpeg(File f,int page){if(!f.exists())return "";
  try(ParcelFileDescriptor fd=ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer renderer=new PdfRenderer(fd)){
   if(page<0||page>=renderer.getPageCount())return "";try(PdfRenderer.Page p=renderer.openPage(page)){
    float scale=Math.min(2f,1300f/Math.max(p.getWidth(),p.getHeight()));int w=Math.max(1,(int)(p.getWidth()*scale)),h=Math.max(1,(int)(p.getHeight()*scale));Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);bitmap.eraseColor(0xffffffff);p.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,84,out);bitmap.recycle();return Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP);
   }
  }catch(Exception ex){return "";}
 }
 public class Bridge{
  @JavascriptInterface public boolean setReminder(int slot,double when,String text){try{return ReminderScheduler.set(MainActivity.this,slot,(long)when,text);}catch(Exception e){return false;}}
  @JavascriptInterface public void removeReminder(int slot){ReminderScheduler.remove(MainActivity.this,slot);}
  @JavascriptInterface public boolean testReminder(){boolean ok=ReminderScheduler.allowed(MainActivity.this);if(ok)ReminderReceiver.show(MainActivity.this,999,"Bu sinov bildirishnomasi. Internetsiz ishlaydi!");return ok;}
  @JavascriptInterface public String reminderStatus(){boolean n=ReminderScheduler.allowed(MainActivity.this),x=ReminderScheduler.canExact(MainActivity.this);return "{\"notifications\":"+n+",\"exact\":"+x+",\"enabled\":"+ReminderScheduler.count(MainActivity.this)+"}";}
  @JavascriptInterface public void requestNotificationPermission(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQUEST_NOTIFICATIONS);});}
  @JavascriptInterface public void openExactAlarmSettings(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=31&&!ReminderScheduler.canExact(MainActivity.this)){try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}}});}
  @JavascriptInterface public boolean pickPdf(int slot){if(slot<0||slot>=400)return false;runOnUiThread(()->{currentPdfSlot=slot;pick("application/pdf",PICK_PDF);});return true;}
  @JavascriptInterface public int pdfPages(int slot){File f=pdfFile(slot);if(!f.exists())return 0;try(ParcelFileDescriptor fd=ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer r=new PdfRenderer(fd)){return r.getPageCount();}catch(Exception e){return 0;}}
  @JavascriptInterface public String pdfPage(int slot,int page){return pageJpeg(pdfFile(slot),page);}
  @JavascriptInterface public boolean pickMedia(int slot){if(slot<32||slot>=349)return false;runOnUiThread(()->{currentMediaSlot=slot;pick(slot<40||slot>=149&&slot<249?"audio/*":"video/*",PICK_MEDIA);});return true;}
  @JavascriptInterface public String mediaUri(int slot){File f=mediaFile(slot);if(!f.exists())return "";return "content://uz.intizom.pro.beta81.imports/"+f.getName();}
  @JavascriptInterface public boolean downloadBook(int slot){return false;} // Bundled books require no download; legacy external catalog unavailable in this source export.
  @JavascriptInterface public int bookDownloadState(int slot){return -1;}
  @JavascriptInterface public boolean openResource(int index){String[] urls={"https://www.ziyouz.com/library/category/113-imom-g-azzoliy-kitoblari","https://api.unilibrary.uz/storage/PublisherResourceFile/319168/images/1685013714.pdf","https://learnenglish.britishcouncil.org/free-resources/listening/a1","https://learnenglishkids.britishcouncil.org/listen-watch/short-stories","https://learningenglish.voanews.com/p/8687.html","https://www.bbc.co.uk/programmes/p0hsrwv5/episodes/downloads","https://www.bbc.co.uk/programmes/p02pc9tn/episodes/downloads","https://www.youtube.com/@bbclearningenglish","https://www.youtube.com/watch?v=sJ7sRSK2in4","https://www.youtube.com/watch?v=-XcALxwXJVY","https://podcasts.apple.com/podcast/id1742967932","https://podcasts.apple.com/us/podcast/6-minute-english/id262026947"};if(index<0||index>=urls.length)return false;try{runOnUiThread(()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(urls[index]))));return true;}catch(Exception e){return false;}}
  @JavascriptInterface public void closeApp(){runOnUiThread(MainActivity.this::finish);}
 }
 @Override public void onBackPressed(){if(web!=null)web.evaluateJavascript("if(window.IntizomBack)window.IntizomBack();",null);else super.onBackPressed();}
 @Override protected void onResume(){super.onResume();ReminderScheduler.restore(this);}
 @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("ReminderNative");web.destroy();}super.onDestroy();}
}
