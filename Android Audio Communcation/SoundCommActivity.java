package edu.vsu.cs.soundcomm;

import edu.vsu.cs.soundcomm.Complex;
import edu.vsu.cs.soundcomm.SignalDetector;
import edu.vsu.cs.soundcomm.SignalMaker;
import edu.vsu.cs.soundcomm.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SoundCommActivity extends Activity {

	//! TODO: place these as configuration parameters (in an XML file)
    private final int PULSE_FREQ0 = 16000;  //14KHz
    private final int PULSE_FREQ1 = 20000;
    private final int PULSE_FREQ_THRESHOLD = 1000; // the threshold in "pulse freq +/- threshold"
    private final double SIGNAL_STRENGTH_MULTIPLIER = 2; // how strong a signal is really a signal
   
    private final int PULSE_DURATION = 1; // 5 seconds
    private final int SAMPLING_FREQ = 44100; //44.1K, desirable to be greater than 18K*2=36K
    private String [] conversionArray;
	private int [] received = new int [8];
	private int index = 0;
	private int counter = 0;		
	private Handler mHandler;
	private TextView tvMsgReceived;
	private TextView messageTitle;
	private EditText msg2send;
	private Button btnSendMsg;
	
	private SignalDetector signalDetector;//= new SignalDetector(PULSE_FREQ, SAMPLING_FREQ, PULSE_FREQ_THRESHOLD, SIGNAL_STRENGTH_MULTIPLIER);
	private SignalMaker signalMaker;//= new SignalMaker(PULSE_FREQ, SAMPLING_FREQ, PULSE_DURATION);
	
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
		       long millisElapsed = SystemClock.uptimeMillis();
		       
		       Complex [] freqs = signalDetector.listenTone();
	    	   // ToDo: demodulate and decode the message and then display the message
		       if(signalDetector.signalExists(freqs)==0) {
		    	   tvMsgReceived.setText("Heard something a 0 at second: " + counter);
		    	   counter++;
		    	   received[index] = 0;
		    	   index++;
		    	   if(index == 8){
		    		   convertChar();
		    		   index = 0;
		    	   }
		       } else if(signalDetector.signalExists(freqs)==1){
		    	   tvMsgReceived.setText("Heard something a 1 at second: "+ counter);
		    	   received[index] = 1;
		    	   counter++;
		    	   index++;
		    	   if(index == 8){
		    		   convertChar();
		    		   index = 0;
		    	   }
		       }
		       // ToDo: to comment it out
		       else {
		    	   tvMsgReceived.setText("Did not hear anything resembling a signal");		    	   
		       }
		       mHandler.postAtTime(this, millisElapsed + 100); //1 seconds 
		   }
		};	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		signalMaker = new SignalMaker(PULSE_FREQ0, PULSE_FREQ1, SAMPLING_FREQ, PULSE_DURATION); 
	    signalDetector = new SignalDetector(PULSE_FREQ0, PULSE_FREQ1, SAMPLING_FREQ, PULSE_FREQ_THRESHOLD, SIGNAL_STRENGTH_MULTIPLIER); 
	        
	    btnSendMsg = (Button)findViewById(R.id.btnSendMsg);
		tvMsgReceived = (TextView)findViewById(R.id.textViewMsgReceived);
	    msg2send = (EditText)findViewById(R.id.editTextMsgToSend);
	    messageTitle = (TextView)findViewById(R.id.TextView01);
		btnSendMsg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
					conversionArray = convertMsg(msg2send.getText().toString());					
					messageTitle.setText(null);
					for(int i = 0; i < conversionArray.length; i++){
						messageTitle.append(String.valueOf(conversionArray[i]));
						for(int j = 0; j < conversionArray[i].length(); j++){
							signalMaker.playTone(String.valueOf(conversionArray[i].charAt(j)));
						}
					}
				}				
	        });		
		

		
        mHandler = new Handler();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 100);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public String [] convertMsg(String msg){
		//for every char in msg
		//byte [] conv = msg.getBytes();
		//int [] char2ascii = new int [msg.length()];
		String [] char2bin = new String[msg.length()+2];
		int ascii;
		char c;
		char2bin[0] = "10111101";
		for(int i = 0; i < msg.length(); i++){
			ascii = msg.charAt(i);
			//ascii  = (int) c;
			//char2ascii[i] = ascii;
			char2bin[i+1] = String.valueOf(Integer.toBinaryString(ascii));
		}
		char2bin[msg.length()+1] = "10011001";
		return char2bin;
	}
	
	public void convertChar(){
		String tempString = "";
		String conv;
		
		for(int i = 0; i < received.length; i++){
			tempString.concat(String.valueOf(received[i]));
		}
		
		int x = Integer.parseInt(tempString);
		conv = Integer.toHexString(x);
		int deci = Integer.parseInt(conv, 16);
		if(deci == 10111101){
			messageTitle.setText(null);
		messageTitle.append(String.valueOf((char)deci));
		}else {
			messageTitle.append(String.valueOf((char)deci));
		}
	}

}
