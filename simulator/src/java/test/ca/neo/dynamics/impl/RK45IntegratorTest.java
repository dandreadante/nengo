/*
 * Created on 30-Mar-07
 */
package ca.neo.dynamics.impl;

import ca.neo.TestUtil;
import ca.neo.config.Configuration;
import ca.neo.config.impl.ConfigurationImpl;
import ca.neo.dynamics.Integrator;
import ca.neo.model.Units;
//import ca.neo.plot.Plotter;
import ca.neo.util.InterpolatorND;
import ca.neo.util.TimeSeries;
import ca.neo.util.impl.LinearInterpolatorND;
import ca.neo.util.impl.TimeSeriesImpl;
import junit.framework.TestCase;

/**
 * Unit tests for RK45Integrator. 
 * 
 * @author Bryan Tripp
 */
public class RK45IntegratorTest extends TestCase {

	public void testIntegrate() {
		VanderPol system = new VanderPol(new float[]{.1f, .1f});
		Integrator integrator = new RK45Integrator();
		TimeSeries input = new TimeSeriesImpl(new float[]{0, 10f}, new float[][]{new float[0], new float[0]}, new Units[]{});
		TimeSeries result = integrator.integrate(system, input);
		
		assertTrue(result.getTimes().length < 60);
		
		//check results against selected hard-coded values from matlab solution ... 
		InterpolatorND interpolator = new LinearInterpolatorND(result);
		float tolerance = 0.005f;
		float[] time2 = interpolator.interpolate(2);
		TestUtil.assertClose(time2[0], 0.053f, tolerance);
		TestUtil.assertClose(time2[1], -0.157f, tolerance);
		float[] time5 = interpolator.interpolate(5);
		TestUtil.assertClose(time5[0], -0.128f, tolerance);
		TestUtil.assertClose(time5[1], 0.223f, tolerance);
		float[] time8 = interpolator.interpolate(8);
		TestUtil.assertClose(time8[0], 0.257f, tolerance);
		TestUtil.assertClose(time8[1], -0.297f, tolerance);
		
//		Plotter.plot(result, "Van der Pol Oscillator");
	}
	
	public static class VanderPol extends AbstractDynamicalSystem {

		private static final long serialVersionUID = 1L;
		private Configuration myConfiguration;

		public VanderPol(float[] state) {
			super(state);
			myConfiguration = new ConfigurationImpl(this);
		}
		
		public VanderPol() {
			this(new float[2]);
		}
		
		/**
		 * @see ca.neo.config.Configurable#getConfiguration()
		 */
		public Configuration getConfiguration() {
			return myConfiguration;
		}

		public float[] f(float t, float[] u) {
			float[] x = getState();
			float epsilon = 0.3f;
			return new float[]{x[1], -x[0] + epsilon*(1 - x[0]*x[0])*x[1]};
		}

		public float[] g(float t, float[] u) {
			return getState();
		}

		public int getInputDimension() {
			return 0;
		}

		public int getOutputDimension() {
			return 2;
		}
	}
	
	//run as application to plot
	public static void main(String[] args) {
		RK45IntegratorTest test = new RK45IntegratorTest();
		test.testIntegrate();
	}

}
