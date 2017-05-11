package com.example.ilse.inventario;
/*By: Ilse Hernandez
* Proyecto realizado en en android studio
* Descripcion: Lectura y escitura de un Tag, con tecnologia NFC*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

@SuppressLint("NewApi")
public class EscribirActivity extends Activity {
    NfcAdapter nfcadapter;
    Context context;
    EditText message,message_Des, message_Res, message_Ubi;
    ToggleButton ReadWriten;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Definimos el layout a usar
        setContentView(R.layout.activity_escribir);
        context = this;
        //Los elementos que vamos a usar en el layout
         message  = (EditText)findViewById(R.id.editText_Num_Iden);
         message_Des = (EditText)findViewById(R.id.editText_Des);
         message_Res = (EditText)findViewById(R.id.editText_Respon);
         message_Ubi = (EditText)findViewById(R.id.editText_Ubi);
         ReadWriten=(ToggleButton)findViewById(R.id.ReadWrite);

         nfcadapter = NfcAdapter.getDefaultAdapter(this);

    }
        private void readTextFromMessage(NdefMessage ndefMessage)
        {
            NdefRecord[] ndefRecords = ndefMessage.getRecords();
            if (ndefRecords != null && ndefRecords.length > 0)
                {
                    NdefRecord ndefRecord = ndefRecords[0];
                    String tagContent = getTextFromNdefRecord(ndefRecord);
                    message.setText(tagContent);
                }
                else
                    {
                         Toast.makeText(this, "No NDEF record found", Toast.LENGTH_SHORT).show();
                    }
        }

        public void onPause()
        {
            super.onPause();
            disableForegroundDispatchSystem();
        }

        public void onResume()
        {
            super.onResume();
            enableForegroundDispatchSystem();
        }

        @Override
        protected void onNewIntent(Intent intent)
         {
            super.onNewIntent(intent);

            if(intent.hasExtra(NfcAdapter.EXTRA_TAG))
            {
                Toast.makeText(this,"NfcIntent",Toast.LENGTH_SHORT).show();

                if (ReadWriten.isChecked())
                {
                    Parcelable []parceables=intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                        if(parceables!=null&& parceables.length>0)
                        {
                            readTextFromMessage((NdefMessage)parceables[0]);


                        }else
                            {
                                Toast.makeText(this,"No NDEF message found!",Toast.LENGTH_SHORT).show();
                            }

                }else
                    {
                        Tag tag =intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                        NdefMessage ndefMessage= createNdefMessage(message.getText()+"");
                        wirteNdefMessage(tag,ndefMessage);

                    }

            }
        }

        private void formtTag(Tag tag,NdefMessage ndefmessage)
        {
            try
            {
                NdefFormatable ndefFromatable= NdefFormatable.get(tag);
                    if (ndefFromatable==null)
                        {
                            Toast.makeText(this,"Tag is not formatable",Toast.LENGTH_SHORT).show();
                        }
                    ndefFromatable.connect();
                    ndefFromatable.format(ndefmessage);
                    ndefFromatable.close();
                    Toast.makeText(this,"Tag write",Toast.LENGTH_SHORT).show();

            }catch (Exception e)
                {
                    Log.e("format tag",e.getMessage());

                }
        }

        private void wirteNdefMessage(Tag tag,NdefMessage ndefMessage)
        {
            try
            {
              if (tag==null)
              {
                Toast.makeText(this,"Tag objeto cannot be null",Toast.LENGTH_SHORT).show();
                       return;
              }
                Ndef ndef=Ndef.get(tag);
                if (ndef==null)
                    {
                        formtTag(tag,ndefMessage);
                    }else
                    {
                        ndef.connect();
                        if (!ndef.isWritable())
                            {
                                Toast.makeText(this,"Tag is not wirtable",Toast.LENGTH_SHORT).show();
                                ndef.close();
                                return;
                            }
                        ndef.writeNdefMessage(ndefMessage);
                        ndef.close();
                        Toast.makeText(this,"Tag write",Toast.LENGTH_SHORT).show();

                    }
            }catch (Exception e){
                Log.e("format tag",e.getMessage());

            }
        }

        public void ReadWriteonClik(View View)
        {
               message.setText("");
        }

        public String getTextFromNdefRecord(NdefRecord ndefRecord)
        {
            String tagContent=null;

            try
            {
                byte[] payload=ndefRecord.getPayload();
                String textEncoding=((payload[0]&128)==0)?"UTF-8":"UTF-16";
                int languagesize=payload[0]&0063;
                tagContent=new String(payload,languagesize +1,payload.length-languagesize-1,textEncoding);

            }catch (UnsupportedEncodingException e)
            {
                Log.e("getTextFromNdefRecord",e.getMessage(),e);
            }
        return tagContent;
        }

        private void enableForegroundDispatchSystem()
        {
            Intent intent=new Intent(this,EscribirActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
            IntentFilter[] intentFilters=new IntentFilter[]{};
            nfcadapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
        }

        private void disableForegroundDispatchSystem()
        {
            nfcadapter.disableForegroundDispatch(this);
        }

        @Nullable
        private NdefRecord createTextRecord(String content)
        {
            try{
                byte[] language;
                language= Locale.getDefault().getLanguage().getBytes("UTF-8");

                final  byte[] text=content.getBytes("UTF-8");
                final int languagesize=language.length;
                final int textLength =text.length;
                final ByteArrayOutputStream payload=new ByteArrayOutputStream(1+languagesize+textLength);

                payload.write((byte)(languagesize & 0x1F));
                payload.write(language,0,languagesize);
                payload.write(text,0,textLength);

                return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload.toByteArray());


            }catch (UnsupportedEncodingException e)
                {
                    Log.e("createTextRecord",e.getMessage());
                }
            return null;
        }

        private  NdefMessage createNdefMessage(String content)
        {
            NdefRecord ndefRecord=createTextRecord(content);
            NdefMessage ndefMessage =new NdefMessage(new NdefRecord[]{ndefRecord});
            return ndefMessage;
        }
}