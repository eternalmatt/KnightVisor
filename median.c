#include <stdio.h>
#include <stdlib.h>

// A must be nonempty
void printlnIntArray(int A [], int length)
{
	printf("{%d", A[0]);
	int i;
	for (i = 1; i < length; ++i)
		printf(", %d", A[i]);
	printf("}\n");
}

/* fill array A with random integers such that 
	for each random integer x, 0 <= x < 256 */
void createRandIntArray(int A [], int length)
{
	srand(time(NULL));
	int i;
	for (i = 0; i < length; i++) {
		A[i] = rand() % 256;
	}
}

// A must be of length 9
int median(int A [])
{
	int mins [6] = {A[0], 256, 256, 256, 256, 256};
	int i, k;
	for (i = 1; i <= 4; ++i) {
		k = i - 1;
		while (A[i] < mins[k]) {
			mins[k+1] = mins[k];
			--k;
			if (k < 0) break;
		}
		mins[k+1] = A[i];
	}
	for (i = 5; i < 9; ++i) {
		k = 4;
		while (A[i] < mins[k]) {
			mins[k+1] = mins[k];
			--k;
			if (k < 0) break;
		}
		mins[k+1] = A[i];
	}
	return mins[4];
}

main()
{
	int A [9];
	createRandIntArray(A, 9);
	printlnIntArray(A, 9);
	
	int med = median(A);
	printf("median: %d\n", med);
	
	return 0;
}