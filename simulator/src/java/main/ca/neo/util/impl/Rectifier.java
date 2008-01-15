/*
 * Created on 27-Jul-2006
 */
package ca.neo.util.impl;

import ca.neo.config.ConfigUtil;
import ca.neo.config.Configuration;
import ca.neo.util.VectorGenerator;

/**
 * Wraps an underlying VectorGenerator, rectifying generated vectors before 
 * they are returned. 
 * 
 * TODO: docs, reconsider default for no-arg constructor
 * 
 * @author Bryan Tripp
 */
public class Rectifier implements VectorGenerator {

	private VectorGenerator myVG;
	private boolean myPositiveFlag = true;
	private Configuration myConfiguration;
	
	/**
	 * @param vg A VectorGenerator to underlie this one (ie to produce non-rectified vectors)
	 */
	public Rectifier(VectorGenerator vg) {
		this(vg, true);
	}
	
	/**
	 * @param vg A VectorGenerator to underlie this one (ie to produce non-rectified vectors)
	 * @param positive True: vectors are rectified; false: vectors are anti-rectified
	 */
	public Rectifier(VectorGenerator vg, boolean positive) {
		myVG = vg;
		myPositiveFlag = positive; 
		myConfiguration = ConfigUtil.defaultConfiguration(this);
	}
	
	public Rectifier() {
		this(new RandomHypersphereVG(), true);
	}
	
	/**
	 * @see ca.neo.config.Configurable#getConfiguration()
	 */
	public Configuration getConfiguration() {
		return myConfiguration;
	}
	
	public VectorGenerator getRectified() {
		return myVG;
	}
	
	public void setRectified(VectorGenerator vg) {
		myVG = vg;
	}
	
	public boolean getPositive() {
		return myPositiveFlag;
	}
	
	public void setPositive(boolean positive) {
		myPositiveFlag = positive;
	}

	/**
	 * @return Rectified version of vector generated by underlying VectorGenerator
	 * 		(all components -> abs value) 
	 *  
	 * @see ca.neo.util.VectorGenerator#genVectors(int, int)
	 */
	public float[][] genVectors(int number, int dimension) {
		float[][] raw = myVG.genVectors(number, dimension);
		float[][] result = new float[raw.length][];
		
		for (int i = 0; i < raw.length; i++) {
			result[i] = new float[raw[i].length];
			for (int j = 0; j < raw[i].length; j++) {
				result[i][j] = myPositiveFlag ? Math.abs(raw[i][j]) : - Math.abs(raw[i][j]);
			}
		}
		
		return result;
	}

}
