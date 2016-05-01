package com.zhihao.quit_cmcc;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
/**
 * Created by Administrator on 2016/4/30 0030.
 */
public class SmsRec_BroadCastReceive extends BroadcastReceiver {
    Context fContext;
    public SmsRec_BroadCastReceive(Context mContext){
        fContext = mContext;
    }
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras() ;
        Object[] pdus = (Object[]) bundle.get("pdus") ;
        SmsMessage[] messages = new SmsMessage[pdus.length] ;
        for(int i = 0; i < messages.length; i++){
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]) ;
          //  Log.i("length",i+"");
        }
        //获取发送方号码
        String address = messages[0].getOriginatingAddress() ;
        if(address.equals("10086222")){
            String fullMessage = "" ;
            for(SmsMessage message : messages){
                fullMessage += message.getMessageBody() ;	//获取短信内容
            }
            Toast.makeText(fContext,fullMessage, Toast.LENGTH_SHORT).show();
            this.abortBroadcast();
        }
    }
}
