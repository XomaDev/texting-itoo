package xyz.kumaraswamy.textingitoo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.KitkatUtil;
import com.google.appinventor.components.runtime.util.LollipopUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import java.util.List;

public class TextingItoo extends AndroidNonvisibleComponent implements OnResumeListener, OnPauseListener {

  private static final String TAG = "TextingItoo";

  private final SmsBroadcastReceiver receiver = new SmsBroadcastReceiver();

  public TextingItoo(ComponentContainer container) {
    super(container.$form());
    if (form.getClass().getSimpleName().equals("FormX")) {
      // the onResume() block will not be called
      // this we have to register it here
      form.registerReceiver(receiver,
              new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }
    form.registerForOnResume(this);
    form.registerForOnPause(this);
  }

  @SimpleEvent
  public void MessageReceived(String phone, String message) {
    EventDispatcher.dispatchEvent(this, "MessageReceived", phone, message);
  }

  private class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "Texting Received");

      String phone = getPhoneNumber(intent);
      String msg = getMessage(intent);

      Log.i(TAG, "Received " + phone + " : " + msg);

      MessageReceived(phone, msg);
    }

    private String getPhoneNumber(Intent intent) {
      String phone = "";

      try {
        if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
          // For Google Voice, phone and msg are stored in String extras. Pretty them up

          phone = intent.getExtras().getString(Texting.PHONE_NUMBER_TAG);
          phone = PhoneNumberUtils.formatNumber(phone);

        } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_KITKAT) {
          // On KitKat or higher, use the convience getMessageFromIntent method.
          List<SmsMessage> messages = KitkatUtil.getMessagesFromIntent(intent);
          for (SmsMessage smsMsg : messages) {
            if (smsMsg != null) {
              // getOriginatingAddress() can throw a NPE if its wrapped message is null, but there
              // isn't an API to check whether this is the case.
              phone = smsMsg.getOriginatingAddress();
              if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
                phone = LollipopUtil.formatNumber(phone);
              } else {
                phone = PhoneNumberUtils.formatNumber(phone);
              }
            }
          }
        } else {
          // On SDK older than KitKat, we have to manually process the PDUs.
          Object[] pdus = (Object[]) intent.getExtras().get("pdus");
          for (Object pdu : pdus) {
            SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
            phone = smsMsg.getOriginatingAddress();
            phone = PhoneNumberUtils.formatNumber(phone);
          }
        }
      } catch(NullPointerException e) {
        Log.w(TAG, "Unable to retrieve originating address from SmsMessage", e);
      }
      return phone;
    }

    /**
     * Extracts the message from the intent.
     * @param intent
     * @return
     */
    private String getMessage(Intent intent) {
      String msg = "";

      try {
        if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
          // For Google Voice, msg is stored in String extras.

          msg = intent.getExtras().getString(Texting.MESSAGE_TAG);

        } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_KITKAT) {
          // On KitKat or higher, use the convience getMessageFromIntent method.
          StringBuilder sb = new StringBuilder();
          List<SmsMessage> messages = KitkatUtil.getMessagesFromIntent(intent);
          for (SmsMessage smsMsg : messages) {
            if (smsMsg != null) {
              sb.append(smsMsg.getMessageBody());
            }
          }
          msg = sb.toString();
        } else {
          // On SDK older than KitKat, we have to manually process the PDUs.
          StringBuilder sb = new StringBuilder();
          Object[] pdus = (Object[]) intent.getExtras().get("pdus");
          for (Object pdu : pdus) {
            SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
            sb.append(smsMsg.getMessageBody());
          }
          msg = sb.toString();
        }
      } catch(NullPointerException e) {
        // getMessageBody() can throw a NPE if its wrapped message is null, but there isn't an
        // API to check whether this is the case.
        Log.w(TAG, "Unable to retrieve message body from SmsMessage", e);
      }
      return msg;
    }
  }

  @Override
  public void onResume() {
    Log.d(TAG, "onResume() called");
    form.registerReceiver(receiver,
            new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause() called");
    form.unregisterReceiver(receiver);
  }
}
