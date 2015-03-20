package edu.vsu.cs.soundcomm;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SignalMaker {
	 private static final String TAG = "SoundBeacon:NoiseMaker";

	
	private AudioTrack Track;
	private Pulse pulse0, pulse1; 
	
	public SignalMaker(int freqOfTone0, int freqOfTone1, int samplingRate, int duration) 
	{
		int minSize = AudioTrack.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);     
		Track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, AudioFormat.CHANNEL_OUT_MONO, 
								AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
		
		try {
			pulse0 = new Pulse((double)Short.MAX_VALUE, 
					(double)freqOfTone0,
					0.0, 
					(double)samplingRate,
					(double)duration);
			pulse1 = new Pulse((double)Short.MAX_VALUE, 
					(double)freqOfTone1,
					0.0, 
					(double)samplingRate,
					(double)duration);
		} catch (SignalException e) {
			Log.e(TAG, e.toString());
			//! TODO: user friendly warning box before killing the app
		}
	}
	
	
	//! Play a previously generated pulse tone. Generate the pulse in place may produce chip sounds
	public void playTone(String signal)
	{
		if(signal.compareTo("0")==0){
		Track.write(pulse0.getIntPulse(), 0, pulse0.getNumberOfSamples()); // write data to audio hardware
		Track.play();   // play an AudioTrack
		}else if(signal.compareTo("1")==0){
			Track.write(pulse1.getIntPulse(), 0, pulse1.getNumberOfSamples()); // write data to audio hardware
			Track.play();   // play an AudioTrack
		}
	}
	
}
