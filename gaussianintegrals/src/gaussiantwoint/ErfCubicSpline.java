package gaussiantwoint;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.special.Erf;

import com.maxeler.maxblox.splines.CubicSpline;
import com.maxeler.maxcompiler.v2.kernelcompiler.KernelBase;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEFix.SignMode;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEVar;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.composite.DFEVector;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.composite.DFEVectorType;

public class ErfCubicSpline extends CubicSpline {
	
	private static final double m_maxSampleX = 4.;

	protected ErfCubicSpline(KernelBase<?> owner, int numBits) {
		super(owner, Math.pow(2, -numBits), numBits);
		// TODO calculate this based on numBits or maxError
	}

	@Override
	protected double derivative(double arg0) {
		return m_maxSampleX * 2.0 * Math.exp(-m_maxSampleX * m_maxSampleX * arg0 * arg0) / Math.sqrt(Math.PI);
	}

	@Override
	protected double function(double arg0) {
		double result = Erf.erf(arg0 * m_maxSampleX);
		//System.out.println("Arg " + (double)(arg0*m_maxSampleX) + " result " + result);
		return result;
//		return Erf.erf(arg0 * m_maxSampleX);
	}

	@Override
	public DFEVector<DFEVar> get(DFEVector<DFEVar> arg,
			DFEVectorType<DFEVar> outputType) {
		double val = 1.0 / m_maxSampleX;
		
		DFEVector<DFEVar> temp = arg * val;
		temp = temp.cast(new DFEVectorType<DFEVar>(dfeFix(1, precision, SignMode.UNSIGNED), arg.getSize()));
		
		DFEVector<DFEVar> result = eval(temp);
		
		List<DFEVar> resultList = new ArrayList<DFEVar>();
		for (int i = 0; i < arg.getSize(); i++) {
			resultList.add(arg.get(i) < m_maxSampleX ? result.get(i) : 1.0);
		}
		
		return DFEVectorType.newInstance(resultList).cast(outputType);
	}

	@Override
	protected double maxFourthDerivative() {
		return 4.5;
	}

	@Override
	protected double maxVal() {
		return 1.0;
	}
	

}
