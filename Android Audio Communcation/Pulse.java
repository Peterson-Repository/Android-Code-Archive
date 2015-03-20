package edu.vsu.cs.soundcomm;

import android.util.Log;

/*! \brief a pulse of form  A * cos(2 * PI * f * t + phi);
 * 
 * 
 * */

public class Pulse 
{
	private static final String TAG = "SoundBeacon:Pulse";

	
    private double amp, freq, phase, samplingRate, duration;
    private double[] samples;
    

    //! create a  pulse 
    public Pulse(double amp, double freq, double phase, double samplingRate, int numSamples) throws SignalException {
    	if (samplingRate < freq * 2)
    		throw new SignalException("Sample Frequency (fs = " 
    				+ samplingRate + ") is less than signal frequency (f = "
    				+ freq + ")");
        this.amp = amp;
        this.freq = freq;
        this.phase = phase;
        this.samplingRate = samplingRate;
        this.duration = 1.0/(double)samplingRate * numSamples;
        
        samples = new double[numSamples];
        
        Log.i(TAG, "Pulse: N = " + numSamples + "\n");
        
        sampling();
    }
    
    //! Create a pulse 
    public Pulse(double amp, double freq, double phase, double samplingRate, double duration) throws SignalException {
    	if (samplingRate < freq * 2)
    		throw new SignalException("Sample Frequency (fs = " 
    				+ samplingRate + ") is less than signal frequency (f = "
    				+ freq + ")");
        this.amp = amp;
        this.freq = freq;
        this.phase = phase;
        this.samplingRate = samplingRate;
        this.duration = duration;
        int numSamples = (int)Math.ceil(duration * samplingRate);
        
        samples = new double[numSamples];
        Log.i(TAG, "Pulse: N = " + numSamples + "\n");
        sampling();
    }

    //! return signal value at time t
    private double getValue(double t) {
        return amp * Math.cos(2 * Math.PI * freq * t  + phase);
    }

    //! sample the signal
    private void sampling() {
        for (int i = 0; i < samples.length; i ++) {
            samples[i] = getValue(1./(double)samplingRate * i);
        }
    }
    
    //! get the duration of the generated pulse
    public double getDuration() { return duration; }
    
    //! get the number of samples
    public int getNumberOfSamples() { return samples.length; }
    
    //! get the samples of the generated pulse
    public double[] getPulse() { return samples; }
    
    //! get the samples of the generated pulse in a series of complex numbers with 0 imaginary
    public Complex[] getComplexPulse() { 
    	Complex[] pulse = new Complex[samples.length];
    	
    	for (int i = 0; i < samples.length; i++) {
    		pulse[i] = new Complex(samples[i], 0.);
    	}
    	
    	return pulse;
    }
    
    //! convert to short integers
    public short[] getIntPulse() {
    	short[] pulse = new short[samples.length];
    	
    	for (int i = 0; i < samples.length; i++) {
    		pulse[i] = (short)samples[i];
    	}
    	
    	return pulse;
    }
}


