package gaussiantwoint;

import com.maxeler.maxcompiler.v2.kernelcompiler.Kernel;
import com.maxeler.maxcompiler.v2.kernelcompiler.KernelParameters;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.KernelMath;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.CounterChain;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.memory.Memory;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEType;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEVar;
import com.maxeler.maxcompiler.v2.utils.MathUtils;

public class GaussianKernel extends Kernel {

	protected GaussianKernel(KernelParameters parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}

	DFEVar maxNx; 
	DFEVar maxNy; 
	DFEVar maxNz; 
	DFEVar maxExp;	
	DFEVar stepx;
	DFEVar stepy;
	DFEVar stepz;

	Memory<DFEVar> alf;



	public  void gaussian( DFEVar Nx1, DFEVar Ny1, DFEVar Nz1 , DFEVar Nx2, DFEVar Ny2, DFEVar Nz2, 
			DFEVar Nx3, DFEVar Ny3, DFEVar Nz3, DFEVar Nx4, DFEVar Ny4, DFEVar Nz4, final int maxMAXExp ) {

		DFEVar result;


		CounterChain chain = control.count.makeCounterChain();
		DFEVar Nexp1 = chain.addCounter(maxExp,1).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp - 1)));
		DFEVar Nexp2 = chain.addCounter(maxExp,1).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp - 1)));		
		DFEVar Nexp3 = chain.addCounter(maxExp,1).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp - 1)));
		DFEVar Nexp4 = chain.addCounter(maxExp,1).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp - 1)));

		DFEVar d12 = ((Nx1-Nx2).cast(stepx.getType()) * stepx * (Nx1-Nx2).cast(stepx.getType()) * stepx) + 
				((Ny1-Ny2).cast(stepy.getType()) * stepy * (Ny1-Ny2).cast(stepy.getType()) * stepy) + 
				((Nz1-Nz2).cast(stepz.getType()) * stepz * (Nz1-Nz2).cast(stepz.getType()) * stepz);
		DFEVar A = alf.read(Nexp1) + alf.read(Nexp2);
		DFEVar red_A = (alf.read(Nexp1) * alf.read(Nexp2))/A;	
		DFEVar CM12x = (alf.read(Nexp1) * ((Nx1).cast(stepx.getType()) * stepx) + alf.read(Nexp2)*((Nx2).cast(stepx.getType()) * stepx))/A;
		DFEVar CM12y = (alf.read(Nexp1) * ((Ny1).cast(stepy.getType()) * stepy) + alf.read(Nexp2)*((Ny2).cast(stepy.getType()) * stepy))/A;
		DFEVar CM12z = (alf.read(Nexp1) * ((Nz1).cast(stepz.getType()) * stepz) + alf.read(Nexp2)*((Nz2).cast(stepz.getType()) * stepz))/A;


		DFEVar d34 = ((Nx3-Nx4).cast(stepx.getType()) * stepx * (Nx3-Nx4).cast(stepx.getType()) * stepx) + 
				((Ny3-Ny4).cast(stepy.getType()) * stepy * (Ny3-Ny4).cast(stepy.getType()) * stepy) + 
				((Nz3-Nz4).cast(stepz.getType()) * stepz * (Nz3-Nz4).cast(stepz.getType()) * stepz);				
		DFEVar B = alf.read(Nexp3) + alf.read(Nexp4);
		DFEVar red_B = (alf.read(Nexp3) * alf.read(Nexp4))/B;			
		DFEVar CM34x = (alf.read(Nexp3) * ((Nx3).cast(stepx.getType()) * stepx) + alf.read(Nexp4)*((Nx4).cast(stepx.getType()) * stepx))/B;
		DFEVar CM34y = (alf.read(Nexp3) * ((Ny3).cast(stepy.getType()) * stepy) + alf.read(Nexp4)*((Ny4).cast(stepy.getType()) * stepy))/B;
		DFEVar CM34z = (alf.read(Nexp3) * ((Nz3).cast(stepz.getType()) * stepz) + alf.read(Nexp4)*((Nz4).cast(stepz.getType()) * stepz))/B;			


		DFEVar dCM = ((CM12x-CM34x) * (CM12x-CM34x)) + 
				((CM12y-CM34y) * (CM12y-CM34y)) + ((CM12z-CM34z) * (CM12z-CM34z));
		DFEVar arg = dCM*A*B/(A+B);
		arg = KernelMath.sqrt(arg);		    

		result = Math.pow(Math.PI, 3)/(A*B*KernelMath.sqrt(A+B));
		result = result * KernelMath.exp(-red_A*d12)*KernelMath.exp(-red_B*d34);	    
//		result *= arg > 10e-4 ? new ErfCubicSpline(this, arg.getType().getTotalBits()).get(arg) / arg : 1.0 ;
		result *= arg > 10e-4 ? KernelMath.erf(arg) / arg : 1.0 ;


		io.output("twoeint", result, result.getType());


	}

	DFEType type = dfeUInt(15);



	public GaussianKernel(final KernelParameters parameters, final int maxMAXExp) {
		super(parameters);

		stepx = io.scalarInput("stepx",dfeFloat(8,24));
		stepy = io.scalarInput("stepy",dfeFloat(8,24));
		stepz = io.scalarInput("stepz",dfeFloat(8,24));
		maxNx = io.scalarInput("maxNx",type);
		maxNy = io.scalarInput("maxNy",type);
		maxNz = io.scalarInput("maxNz",type);
		maxExp = io.scalarInput("maxExp", type); 

		// MODIFIED PAVEL
		//DFEVar numTicks = io.scalarInput("numTicks", dfeUInt(64));
		// MODIFIED PAVEL

		alf = mem.alloc(dfeFloat(8,24), maxMAXExp );
		alf.mapToCPU("alf");	


		//DFEVar maxGLOB = maxNx * maxNy * maxNz; 

		DFEVar enable = control.count.pulse(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp);
		//DFEVar enable = control.count.pulse(maxExp*maxExp*maxExp*maxExp);
		CounterChain chain = control.count.makeCounterChain(enable);
		DFEVar Nx1 = chain.addCounter(maxNx,1).cast(dfeInt(maxNx.getType().getTotalBits()+1));
		DFEVar Ny1 = chain.addCounter(maxNy,1).cast(dfeInt(maxNy.getType().getTotalBits()+1));
		DFEVar Nz1 = chain.addCounter(maxNz,1).cast(dfeInt(maxNz.getType().getTotalBits()+1));
		//DFEVar GLOB = chain.addCounter(maxGLOB, UnrollFactor).cast(dfeInt(maxGLOB.getType().getTotalBits()+1));
		DFEVar Nx2 = chain.addCounter(maxNx,1).cast(dfeInt(maxNx.getType().getTotalBits()+1));
		DFEVar Ny2 = chain.addCounter(maxNy,1).cast(dfeInt(maxNy.getType().getTotalBits()+1));
		DFEVar Nz2 = chain.addCounter(maxNz,1).cast(dfeInt(maxNz.getType().getTotalBits()+1));
		DFEVar Nx3 = chain.addCounter(maxNx,1).cast(dfeInt(maxNx.getType().getTotalBits()+1));
		DFEVar Ny3 = chain.addCounter(maxNy,1).cast(dfeInt(maxNy.getType().getTotalBits()+1));
		DFEVar Nz3 = chain.addCounter(maxNz,1).cast(dfeInt(maxNz.getType().getTotalBits()+1));
		DFEVar Nx4 = chain.addCounter(maxNx,1).cast(dfeInt(maxNx.getType().getTotalBits()+1));
		DFEVar Ny4 = chain.addCounter(maxNy,1).cast(dfeInt(maxNy.getType().getTotalBits()+1));
		DFEVar Nz4 = chain.addCounter(maxNz,1).cast(dfeInt(maxNz.getType().getTotalBits()+1));


		//maxExp = maxExp.cast(dfeInt(type.getTotalBits()+1));
		maxNx = maxNx.cast(dfeInt(type.getTotalBits()+1));
		maxNy = maxNy.cast(dfeInt(type.getTotalBits()+1));
		maxNz = maxNz.cast(dfeInt(type.getTotalBits()+1));


		gaussian(Nx1, Ny1, Nz1, 
				 Nx2, Ny2, Nz2, 
				 Nx3, Ny3, Nz3, 
				 Nx4, Ny4, Nz4, maxMAXExp);	// NEEDS TO GIVE A VECTOR IN FMEM AS OUTPUT 


		// io.output("twoeint", output, output.getType());
		// MODIFIED PAVEL
		//AspectChangeIO acio = new AspectChangeIO(this, MathUtils.nextMultiple(output.getType().getTotalBits(), 128));

		//final DFEVar cycleCounter = control.count.simpleCounter(48);
		//acio.output("twoeint", output, constant.var(true), cycleCounter === numTicks.cast(dfeUInt(48)) - 1);
		// MODIFIED PAVEL
	}



}
