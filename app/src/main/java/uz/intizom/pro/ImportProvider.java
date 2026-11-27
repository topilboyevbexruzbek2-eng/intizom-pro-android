package uz.intizom.pro;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
public class ImportProvider extends ContentProvider{
 @Override public boolean onCreate(){return true;}
 @Override public String getType(Uri uri){String n=uri.getLastPathSegment();return n!=null&&n.endsWith(".mp4")?"video/mp4":"audio/mpeg";}
 @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{String n=uri.getLastPathSegment();if(n==null||!n.matches("(?:audio|video)-[0-9]{1,3}\\.(?:mp3|m4a|mp4)"))throw new FileNotFoundException();File f=new File(getContext().getFilesDir(),"media/"+n);return ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);}
 @Override public Cursor query(Uri u,String[] p,String s,String[] a,String so){return null;}
 @Override public Uri insert(Uri u,ContentValues v){return null;}
 @Override public int delete(Uri u,String s,String[] a){return 0;}
 @Override public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
