#include <stdio.h>
#include <stdint.h>

#include "Maxfiles.h"

#define UNROLL_FACTOR gaussianintegrals_UnrollFactor

void run(size_t Nx, size_t Ny, size_t Nz, double stepx, double stepy, double stepz, size_t nExp, double *alf) {
    size_t num_functions = Nx * Ny * Nz * nExp;
    size_t num_cycles = num_functions * num_functions * num_functions * num_functions;

    int ticks = (num_cycles+UNROLL_FACTOR-1)/UNROLL_FACTOR;

    float *outstream_twoeint = malloc(ticks * UNROLL_FACTOR * sizeof(float));

    printf("Running on DFE...\n");
    printf("Parameters: %zu %zu %zu, %lf %lf %lf, %zu\n", Nx, Ny, Nz, stepx, stepy, stepz, nExp);
    fflush(stdout);




    gaussianintegrals(
    	ticks,
    	nExp,
    	Nx,
    	Ny,
    	Nz,
    	stepx,
		stepy,
		stepz,
    	outstream_twoeint,
		ticks * UNROLL_FACTOR * sizeof(float),
    	alf
	);

    printf("Writing to file...\n");
    fflush(stdout);

    FILE *file = fopen("foo23.txt", "w");

    for (size_t i = 0; i < num_cycles; i++) {
    	fprintf(file, "%9.7e\n", outstream_twoeint[i]);
    }
    fclose(file);
    printf("Done.\n");
    fflush(stdout);

}

void read_input(char *filename, int *Nx, int *Ny, int *Nz,
		double *stepx, double *stepy, double *stepz, int *nExp, double **alf) {
	FILE *file = fopen(filename, "r");
	fscanf(file, "%d %d %d\n", Nx, Ny, Nz);
	fscanf(file, "%lf %lf %lf\n", stepx, stepy, stepz);
	fscanf(file, "%d\n", nExp);

	*alf = malloc(*nExp * sizeof(double));
	for (size_t i = 0; i < (size_t)(*nExp); i++) {
		fscanf(file, "%lf\n", &((*alf)[i]));
	}
	fclose(file);
}

int main(int argc, char *argv[]) {

	if (argc < 2) {
		printf("Usage: %s <filename>\n", argv[0]);
		exit(1);
	}

	int Nx, Ny, Nz, nExp;
	double stepx, stepy, stepz;
	double *alf;

	read_input(argv[1], &Nx, &Ny, &Nz, &stepx, &stepy, &stepz, &nExp, &alf);
	run(Nx, Ny, Nz, stepx, stepy, stepz, nExp, alf);
}

