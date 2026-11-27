package uz.intizom.pro;
import android.app.*;
import android.content.*;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
 public static void show(Context c,int slot,String message){
  if(!ReminderScheduler.allowed(c))return;
  NotificationManager mgr=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel("intizom_offline_73","INTIZOM PRO eslatmalari",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Internetsiz kunlik eslatmalar");mgr.createNotificationChannel(ch);}
  PendingIntent open=PendingIntent.getActivity(c,777,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,"intizom_offline_73"):new Notification.Builder(c);
  builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("INTIZOM PRO · Eslatma").setContentText(message).setContentIntent(open).setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE).setPriority(Notification.PRIORITY_HIGH);
  mgr.notify(1000+slot,builder.build());
 }
 @Override public void onReceive(Context c,Intent i){int slot=i.getIntExtra("slot",-1);if(slot<0||slot>=32||!ReminderScheduler.prefs(c).getBoolean("on_"+slot,false))return;show(c,slot,ReminderScheduler.text(c,slot));ReminderScheduler.schedule(c,slot);}
}
