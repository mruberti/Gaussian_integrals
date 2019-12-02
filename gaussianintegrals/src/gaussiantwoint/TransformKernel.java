package gaussiantwoint;

import maxpower.kernel.arithmetic.FloatingPointAccumulator;

import com.maxeler.maxcompiler.v2.kernelcompiler.Kernel;
import com.maxeler.maxcompiler.v2.kernelcompiler.KernelParameters;
import com.maxeler.maxcompiler.v2.kernelcompiler.op_management.FixOpBitSizeMode;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Count;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Count.Counter;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Count.WrapMode;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.CounterChain;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Stream.OffsetExpr;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.memory.Memory;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEType;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEVar;
import com.maxeler.maxcompiler.v2.utils.MathUtils;
import com.maxeler.photon.hw.FixOperatorFactory;

public class TransformKernel extends Kernel {

	protected TransformKernel(KernelParameters parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}


	DFEVar maxExp;	

	Memory<DFEVar> coeff;
	Memory<DFEVar> memA;

	public  void transform( final int maxMAXExp) {

		Memory<DFEVar> memB = mem.alloc(dfeFloat(8,24), maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp );

		
		final int bits = MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp - 1);
		
		Count.Params  paramsOne = control.count.makeParams(32).withMax(maxExp*maxExp*maxExp*maxExp).withWrapMode(WrapMode.STOP_AT_MAX);
		Counter initialCounter = control.count.makeCounter(paramsOne);
		DFEVar counter = initialCounter.getCount()/*.cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp -1)))*/;
		
		//DFEVar initialCounter = control.count.simpleCounter(32, maxExp*maxExp*maxExp*maxExp);
        
		
		
		
		// counter === maxExp^4 - 1 
//		optimization.pushFixOpMode);
		DFEVar enable = counter===(maxExp*maxExp*maxExp*maxExp).cast(counter.getType());
		//CounterChain chain = control.count.makeCounterChain(constant.var(false));
		CounterChain chain = control.count.makeCounterChain(enable);
		DFEVar nTransform = chain.addCounter(4,1);
		DFEVar indt = chain.addCounter(maxExp,1).cast(type);
		DFEVar ind1 = chain.addCounter(maxExp,1).cast(type);
		DFEVar ind2 = chain.addCounter(maxExp,1).cast(type);		
		DFEVar ind3 = chain.addCounter(maxExp,1).cast(type);
		DFEVar inds = chain.addCounter(maxExp,1).cast(type);
		
		//DFEVar enable = nTransform===0 & indt===0;
		//DFEVar twoeint = io.input("twoeint",dfeFloat(8,24), enable);
		//debug.simPrintf(enable, "twoeint %f\n", twoeint);
		
		
		DFEVar twoeint = io.input("twoeint",dfeFloat(8,24), ~enable);
		//debug.simPrintf("enable: %d count: %d\n", enable, counter);
		debug.simPrintf(~enable, "twoeint %f\n", twoeint);
		
		
//		System.out.println(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp));

		DFEVar flip = ~nTransform[0];
		
		DFEVar counterOut = ((indt)*maxExp*maxExp*maxExp + (ind1)*maxExp*maxExp + (ind2)*maxExp + ind3)
				.cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp -1))); 

		DFEVar counterIn = ((ind1)*maxExp*maxExp*maxExp + (ind2)*maxExp*maxExp + (ind3)*maxExp + inds)
				.cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp -1 )));
		
		
		DFEVar input = flip ? memA.read(counterIn) : memB.read(counterIn);
		//input = enable ? twoeint : input;
		
		DFEVar flip2 = nTransform===0 & indt===0;
		DFEVar flip3 = indt===0;
		//debug.simPrintf(enable & flip2, "memA read %f\n", memA.read(counterIn));
		
		
//		TODO: to be removed
//		memA.write(counterIn, twoeint, enable);
		
		DFEVar term = coeff.read((indt*maxExp+inds).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp - 1)))) * input;

		
		DFEVar runningSum = FloatingPointAccumulator.accumulateFixedLength(term, enable, maxExp, true); // summand, enable, numVal, correctOnEveryCycle)
		//DFEVar runningSum = FloatingPointAccumulator.accumulateFixedLength(term, constant.var(true), maxExp, true); // summand, enable, numVal, correctOnEveryCycle)

		debug.simPrintf(enable, "nTransform: %d indt: %d input: %f\n coeff: %f\n term: %f\n sum: %f\n", nTransform, indt, input,
				coeff.read((indt*maxExp+inds).cast(dfeUInt(MathUtils.bitsToRepresentUnsigned(maxMAXExp*maxMAXExp - 1)))), term, runningSum);
		
//		debug.simPrintf( nTransform === 0 ,"runningSum %f\n", runningSum);

		final OffsetExpr latency = stream.makeOffsetAutoLoop("helloworld", 30, 80);
		final DFEVar latencyVal = latency.getDFEVar(this, dfeUInt(48));

		DFEVar currCycle = control.count.simpleCounter(48);

		
		DFEVar address = stream.offset(counterOut, -latency);
//		~enable ? counter.cast(dfeUInt(bits)) : 
		address = ~enable ? counter.cast(dfeUInt(bits)) : address;
		//final DFEVar address = stream.offset(enable ? counterIn : counterOut, -latency);

		DFEVar dataToWrite = stream.offset(runningSum, -latency);
		dataToWrite = ~enable ? twoeint : dataToWrite; 
		//final DFEVar dataToWrite = stream.offset(~enable ? twoeint : runningSum, -latency);
		
		final DFEVar addressB = stream.offset( counterOut, -latency);

		final DFEVar dataToWriteB = stream.offset( runningSum, -latency);
		
		//final DFEVar enableA = stream.offset( enable ? enable : (~flip & ( inds === (maxExp -1) )), -latency);
		final DFEVar enableA = stream.offset( enable & ~flip & ( inds === (maxExp -1) ), -latency);
		
		final DFEVar enableB = stream.offset( enable & flip & ( inds === (maxExp -1) ), -latency);
	
		memA.write(address, dataToWrite, ~enable | (enableA & (currCycle > latencyVal)));
		//memA.write(address, dataToWrite, enable | (enableA & (currCycle > latencyVal)));
		debug.simPrintf(/*~stream.offset(enable,-1),*/ "enable %d address %d memA wrote %f\n", 
				~enable | (enableA & (currCycle > latencyVal)), address, dataToWrite); //, memA.read(stream.offset(counter,-1).cast(dfeUInt(bits))));
		
		
		memB.write(addressB, dataToWriteB, enableB & (currCycle > latencyVal));	
		//memB.write(addressB, dataToWriteB, enableB & (currCycle > latencyVal));	
		
		debug.simPrintf(enableA, "memA wrote final %f\n", dataToWrite);
		debug.simPrintf(enableB, "memB wrote final %f\n", dataToWriteB);
		io.output("toCPU", runningSum, dfeFloat(8,24), (nTransform === 3) & ( inds === (maxExp -1) ) );//& (currCycle > latencyVal) );

	}

	DFEType type = dfeUInt(15);



	public TransformKernel(final KernelParameters parameters, final int maxMAXExp) {
		super(parameters);

		maxExp = io.scalarInput("maxExp", type); 


		coeff = mem.alloc(dfeFloat(8,24), maxMAXExp*maxMAXExp );
		coeff.mapToCPU("coeff");
		memA = mem.alloc(dfeFloat(8,24), maxMAXExp*maxMAXExp*maxMAXExp*maxMAXExp );
//		memA.mapToCPU("memA");

		//maxExp = maxExp.cast(dfeInt(type.getTotalBits()+1));

		
		transform(maxMAXExp);


	}	


}
