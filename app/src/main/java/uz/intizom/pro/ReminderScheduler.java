package uz.intizom.pro;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

/** All reminders are local and survive lost internet, reboot and a changed time zone. */
public final class ReminderScheduler {
 private static final String PREF="offline_reminders_73";
 private ReminderScheduler(){}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
 static PendingIntent pending(Context c,int slot){Intent i=new Intent(c,ReminderReceiver.class);i.putExtra("slot",slot);return PendingIntent.getBroadcast(c,1000+slot,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static boolean allowed(Context c){if(Build.VERSION.SDK_INT>=33)return c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED;return ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();}
 static boolean canExact(Context c){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);return Build.VERSION.SDK_INT<31||a.canScheduleExactAlarms();}
 static long nextLocal(int hour,int minute){Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,hour);cal.set(Calendar.MINUTE,minute);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_MONTH,1);return cal.getTimeInMillis();}
 static synchronized boolean set(Context c,int slot,long requested,String text){
  if(slot<0||slot>=32)return false;
  Calendar time=Calendar.getInstance();time.setTimeInMillis(requested);
  int hour=time.get(Calendar.HOUR_OF_DAY),minute=time.get(Calendar.MINUTE);
  prefs(c).edit().putBoolean("on_"+slot,true).putInt("h_"+slot,hour).putInt("m_"+slot,minute).putString("text_"+slot,text==null?"Vazifani bajarish vaqti!":text.substring(0,Math.min(text.length(),160))).apply();
  schedule(c,slot);return allowed(c);
 }
 static synchronized void schedule(Context c,int slot){
  if(!prefs(c).getBoolean("on_"+slot,false))return;
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent p=pending(c,slot);
  a.cancel(p);long at=nextLocal(prefs(c).getInt("h_"+slot,8),prefs(c).getInt("m_"+slot,0));
  // Unlike setInexactRepeating, schedule each day's alarm individually.
  // Android 12+ without exact alarm access uses an inexact Doze-aware fallback.
  if(Build.VERSION.SDK_INT>=31){if(a.canScheduleExactAlarms())a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,p);else a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,p);}
  else if(Build.VERSION.SDK_INT>=23)a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,p);
  else if(Build.VERSION.SDK_INT>=19)a.setExact(AlarmManager.RTC_WAKEUP,at,p);
  else a.set(AlarmManager.RTC_WAKEUP,at,p);
 }
 static synchronized void remove(Context c,int slot){if(slot<0||slot>=32)return;prefs(c).edit().putBoolean("on_"+slot,false).apply();((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pending(c,slot));}
 static synchronized void restore(Context c){for(int i=0;i<32;i++)if(prefs(c).getBoolean("on_"+i,false))schedule(c,i);}
 static int count(Context c){int n=0;for(int i=0;i<32;i++)if(prefs(c).getBoolean("on_"+i,false))n++;return n;}
 static String text(Context c,int slot){return prefs(c).getString("text_"+slot,"Bugungi rejangizni bajaring!");}
}
