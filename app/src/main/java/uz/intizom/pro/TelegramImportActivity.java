package uz.intizom.pro;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import java.io.*;
public class TelegramImportActivity extends Activity{
 @Override public void onCreate(Bundle s){super.onCreate(s);try{Intent i=getIntent();Uri u=i.getParcelableExtra(Intent.EXTRA_STREAM);String mime=i.getType();if(u!=null&&mime!=null&&(mime.startsWith("audio/")||mime.startsWith("video/"))){boolean audio=mime.startsWith("audio/");File dir=new File(getFilesDir(),"media");dir.mkdirs();int slot=audio?149:249;for(;slot<(audio?249:349);slot++){File f=new File(dir,(audio?"audio-":"video-")+slot+(audio?".mp3":".mp4"));if(!f.exists()){try(InputStream in=getContentResolver().openInputStream(u);OutputStream out=new FileOutputStream(f)){byte[] b=new byte[16384];int n;long sum=0;while((n=in.read(b))!=-1){sum+=n;if(sum>150000000)throw new IOException("too large");out.write(b,0,n);}}break;}}}}catch(Exception ignored){}startActivity(new Intent(this,MainActivity.class));finish();}
}
