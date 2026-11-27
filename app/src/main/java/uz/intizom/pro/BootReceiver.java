package uz.intizom.pro;
import android.content.*;
public class BootReceiver extends BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){String a=i.getAction();if(Intent.ACTION_BOOT_COMPLETED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)||Intent.ACTION_TIMEZONE_CHANGED.equals(a))ReminderScheduler.restore(c);}
}
