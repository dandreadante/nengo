/*
 * Created on 1-Jun-2006
 */
package ca.nengo.dynamics.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Units;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.LinearInterpolatorND;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Euler's method of numerical integration: x(t+h) ~ x(t) + h*x'(t)
 * 
 * TODO: test
 * TODO: should there be some means for aborting early (aside from exceptions, e.g. if output converges to constant)?  
 * 
 * @author Bryan Tripp
 */
public class EulerIntegrator implements Integrator {

	private static final long serialVersionUID = 1L;

	//shrink factor to avoid possible very small step at end due to float comparison 
	//TODO: solve this problem more robustly
	private static final float SHRINK = .99999f; 
	
	private float h;
	
	public EulerIntegrator(float stepSize) {
		h = stepSize;
	}
	
	/**
	 * Uses default step size of .0001
	 */
	public EulerIntegrator() {
		this(.0001f);
	}
	
	public float getStepSize() {
		return h;
	}
	
	public void setStepSize(float stepSize) {
		h = stepSize;
	}

	/**
	 * Linear interpolation is performed between given input points. 
	 * 
	 * @see ca.nengo.dynamics.Integrator#integrate(ca.nengo.dynamics.DynamicalSystem, ca.nengo.util.TimeSeries)
	 */
	public TimeSeries integrate(DynamicalSystem system, TimeSeries input) {
		float[] inTimes = input.getTimes();
		float timespan = inTimes[inTimes.length-1] - inTimes[0];
		int steps = (int) Math.ceil(timespan*SHRINK / h);

		LinearInterpolatorND interpolator = new LinearInterpolatorND(input);
		
		float[] times = new float[steps+1];
		float[][] values = new float[steps+1][];
		times[0] = inTimes[0];
		values[0] = system.g(times[0], input.getValues()[0]);

		float t = inTimes[0];
		for (int i = 1; i <= steps; i++) {
			float dt = (i < steps) ? h : (inTimes[inTimes.length-1] - t);
			t = t + dt;
			times[i] = t;
			
			float[] u = interpolator.interpolate(t);
			float[] dxdt = system.f(t, u);
			system.setState(MU.sum(system.getState(), MU.prod(dxdt, dt)));
			values[i] = system.g(t, u);
		}
		
		Units[] units = new Units[system.getOutputDimension()];
		for (int i = 0; i < units.length; i++) {
			units[i] = system.getOutputUnits(i);
		}
		
		return new TimeSeriesImpl(times, values, units);
	}

	@Override
	public Integrator clone() throws CloneNotSupportedException {
		return (Integrator) super.clone();
	}
	
	
}