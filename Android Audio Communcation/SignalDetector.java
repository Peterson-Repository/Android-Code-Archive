package edu.vsu.cs.soundcomm;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
// import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D; 
import android.util.Log;

public class SignalDetector {
	private static final String TAG = "SoundBeacon:NoiseDector";
	
	private AudioRecord AudioInput;
	private short[] buffer;
	private int minBufferSize;
	private int readLength;
	
	private int freqOfTone0, freqOfTone1;
	private int samplingFreq;
	private int freqErrorTolerance;
	private double signalStrengthMultiplier; 
	
	public SignalDetector(int freqOfTone0, int freqOfTone1, int samplingFreq, int freqErrorTolerance, double signalStrengthMultiplier) {
		this.freqOfTone0 = freqOfTone0;
		this.freqOfTone1 = freqOfTone1;
		this.samplingFreq = samplingFreq;
		this.freqErrorTolerance = freqErrorTolerance;
		this.signalStrengthMultiplier = signalStrengthMultiplier;

		minBufferSize = AudioRecord.getMinBufferSize(samplingFreq, AudioFormat.CHANNEL_IN_MONO , AudioFormat.ENCODING_PCM_16BIT);
		// if (minBufferSize < 0) minBufferSize = 44100;
		Log.e("SoundComm:SignalDetector", "buffer size = " + minBufferSize);
		AudioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingFreq, AudioFormat.CHANNEL_IN_MONO , 
								AudioFormat.ENCODING_PCM_16BIT, 2*minBufferSize);
		
		readLength = (int)Math.pow(2, Math.ceil(Math.log((double)minBufferSize)/Math.log(2.)));
		buffer = new short[readLength];
	}

	public Complex[] listenTone() {
		// ToDo: For performance use edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D; 
		// DoubleFFT_1D fft = new DoubleFFT_1D(44100);
		AudioInput.startRecording();
		AudioInput.read(buffer, 0, readLength);
		
		Complex [] input = new Complex[readLength];
		for(int i=0; i<readLength; i++)	{
			input[i] = new Complex(buffer[i], 0);
		}
		
		// ToDo: For performance use edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
		Complex [] freqs = FFT.fft(input);
		return FFT.shift(freqs);
	}
	
	public int signalExists(Complex [] freqs) 
	{
		// for debug
		Spectrum spectrum = new Spectrum(freqs);
		
		//! determine if dominant frequency exists
		Log.i(TAG, "Dominant frequency: " + spectrum.getStrongestComponent().toString());
		Log.i(TAG, "Dominant frequency: Singal with mean = " + spectrum.getMeanAmp() + "  stdev = " + spectrum.getAmpStdev());
		if (spectrum.getStrongestComponent().getAmptitude() - spectrum.getMeanAmp() >= signalStrengthMultiplier * spectrum.getAmpStdev()) {
			//! determine if the frequency is desired.
			double f = spectrum.getStrongestComponent().getFrequency();
			if (Math.abs(f - freqOfTone0) <= freqErrorTolerance) {
				Log.i(TAG, "Dominant frequency match target: " + freqOfTone0 + "\n");
				return 0;
			} else if (Math.abs(f - freqOfTone1) <= freqErrorTolerance) {
				Log.i(TAG, "Dominant frequency match target: " + freqOfTone1 + "\n");
				return 1;
			} 
		}
		return 2;
	}
	

	private class FrequencyComponent {
		private double freq, amp;
		
		public FrequencyComponent (double f, double a) {
			freq = f;
			amp = a;
		}
			
		public String toString() {
			String out = "";
			out += "<Freq = " + freq + "\tamp = " + amp + ">";
			return out;
		}
		
		public double getAmptitude() { return amp; }
		
		public double getFrequency() { return freq; }
	}
	
	private class Spectrum {
		
		private FrequencyComponent[] freqCompnents;
		private double meanAmp;
		private double ampStdev;
		private int idxOfMaxAmp;
		
		public Spectrum(Complex[] freqs) {
			// for debug
			double m1 = 0, m2, s1 = 0, s2;
			freqCompnents = new FrequencyComponent[freqs.length/2];
			for (int i = 0; i < freqs.length/2; i ++) {
				double f = (freqs.length/2. - i) / freqs.length * samplingFreq;
				double a = 2 * freqs[i].abs() / freqs.length;
				freqCompnents[i] = new FrequencyComponent(f, a);
				
				//! Knuth recommended method for computing mean & standard deviation
				if (i == 0 ) {
					m1 = a;
					s1 = 0;
					idxOfMaxAmp = 0;
				} else {
					m2 = m1 + (a - m1)/(i+1.);
					s2 = s1 + (a - m1)*(a - m2);
					m1 = m2;
					s1 = s2;
					
					if (a > freqCompnents[idxOfMaxAmp].getAmptitude()) {
						idxOfMaxAmp = i;
					}
				}				
			}
			meanAmp = m1;
			ampStdev = Math.sqrt(s1)/(freqs.length/2. - 1.);
			
		}
		
		public double getMeanAmp() { return meanAmp; }
		
		public double getAmpStdev() { return ampStdev; }
		
		public FrequencyComponent getStrongestComponent() { return freqCompnents[idxOfMaxAmp]; }

	}
}
