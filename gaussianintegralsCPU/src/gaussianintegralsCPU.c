#include <stdio.h>
#include <stdint.h>

#include "Maxfiles.h"
//
//void gaussianintegrals(
//	uint64_t ticks_GaussianKernel,
//	uint64_t ticks_TransformKernel,
//	uint64_t inscalar_GaussianKernel_maxExp,
//	uint64_t inscalar_GaussianKernel_maxNx,
//	uint64_t inscalar_GaussianKernel_maxNy,
//	uint64_t inscalar_GaussianKernel_maxNz,
//	double inscalar_GaussianKernel_stepx,
//	double inscalar_GaussianKernel_stepy,
//	double inscalar_GaussianKernel_stepz,
//	uint64_t inscalar_TransformKernel_maxExp,
//	void *outstream_toCPU,
//	size_t outstream_size_toCPU,
//	const double *inmem_GaussianKernel_alf,
//	const double *inmem_TransformKernel_coeff);

void run(size_t Nx, size_t Ny, size_t Nz, double stepx, double stepy, double stepz, size_t nExp, double *alf, double *coeff) {
//    size_t num_functions = Nx * Ny * Nz * nExp;
//    size_t num_cycles = num_functions * num_functions * num_functions * num_functions * num_functions * 4;

//    int ticks = (num_cycles+UNROLL_FACTOR-1)/UNROLL_FACTOR;
    int ticks_g = nExp * nExp * nExp * nExp;
    int ticks_t = nExp * nExp * nExp * nExp * nExp * 4 + ticks_g;


    //float *outstream_twoeint = malloc(ticks * UNROLL_FACTOR * sizeof(float));
    float *output = malloc(nExp * nExp * nExp * nExp * sizeof(float));
    printf("Running on DFE...\n");
    printf("Parameters: %zu %zu %zu, %lf %lf %lf, %zu\n", Nx, Ny, Nz, stepx, stepy, stepz, nExp);
    fflush(stdout);

    gaussianintegrals(ticks_g, ticks_t, nExp, Nx, Ny, Nz, stepx, stepy, stepz, nExp, output, nExp*nExp*nExp*nExp*sizeof(float), alf, coeff);

//    max_file_t* max_file_gaussian = gaussianintegrals_init();
//    max_engine_t* engine = max_load(max_file_gaussian, "*");

//    max_actions_t* actions = max_actions_init(max_file_gaussian, NULL);



//	max_set_ticks   ( actions, "gaussianintegralsKernel", ticks );
//	max_queue_input ( actions, "x", x, N * sizeof(float) );

//	max_queue_output( actions, "twoeint", outstream_twoeint,
//			num_functions * num_functions * num_functions * num_functions * UNROLL_FACTOR * sizeof(float));

//	max_set_mem_range_double( actions, "gaussianintegralsKernel",
//					"coeff", 0, 16, coeff);

//	max_set_mem_range_double(actions, "gaussianintegralsKernel",
//			"memA", 0, 256, memA);
	//max_set_mem_range_double(actions, "gaussianintegralsKernel",
	//			"memB", 0, 160000, calloc(160000, sizeof(float)));



//	max_set_uint64t( actions, "gaussianintegralsKernel", "maxExp", nExp);
//
//
//	max_run ( engine, actions );
//
//	max_actions_free( actions );
//	max_unload( engine );

    printf("Writing to file...\n");
    fflush(stdout);

    FILE *file = fopen("output.txt", "w");

    for (size_t i = 0; i <nExp*nExp*nExp*nExp; i++) {
    	//fprintf(file, "%9.7e\n", outstream_twoeint[i]);
    	fprintf(file, "%lf\n", output[i]);
    }
    fclose(file);
    printf("Done.\n");
    fflush(stdout);

}

void read_input(char *filename, int *Nx, int *Ny, int *Nz,
		double *stepx, double *stepy, double *stepz, int *nExp, double **alf, double **coeff) {
	FILE *file = fopen(filename, "r");
	fscanf(file, "%d %d %d\n", Nx, Ny, Nz);
	fscanf(file, "%lf %lf %lf\n", stepx, stepy, stepz);
	fscanf(file, "%d\n", nExp);

	*alf = malloc(*nExp * sizeof(double));
	for (size_t i = 0; i < (size_t)(*nExp); i++) {
		fscanf(file, "%lf\n", &((*alf)[i]));
	}


	//*coeff = calloc((*nExp)*(*nExp), sizeof(double));
	*coeff = calloc(400, sizeof(double));
		for (size_t i = 0; i < (size_t)(*nExp)*(*nExp); i++) {
			fscanf(file, "%lf\n", &((*coeff)[i]));
		}

	fclose(file);
//	file = fopen("twoeint.txt", "r");

	//*memA = (double*) calloc((*nExp)*(*nExp)*(*nExp)*(*nExp), sizeof(double));

//	*memA = (double*) calloc(160000, sizeof(double));
//		for (size_t i = 0; i < (size_t)(*nExp)*(*nExp)*(*nExp)*(*nExp); i++) {
//			fscanf(file, "%lf\n", &((*memA)[i]));
//		}

//	fclose(file);
}

int main(int argc, char *argv[]) {

	if (argc < 2) {
		printf("Usage: %s <filename>\n", argv[0]);
		exit(1);
	}

	int Nx, Ny, Nz, nExp;
	double stepx, stepy, stepz;
	double *alf;
	double *coeff;
//	double *memA;

	read_input(argv[1], &Nx, &Ny, &Nz, &stepx, &stepy, &stepz, &nExp, &alf, &coeff);
	run(Nx, Ny, Nz, stepx, stepy, stepz, nExp, alf, coeff);
}

