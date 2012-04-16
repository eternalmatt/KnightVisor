#include <math.h>

typedef unsigned char pixel; // a pixel has range [0..255]

int thresholdValue(pixel f [], int nPixels)
{	
	// calculate histogram
	pixel histogram [256];
	int i;
	for (i = 0; i < nPixels; ++i)
		++histogram[f[i]];
	
	// calculate cumulative sum and weighted cumulative sum
	pixel cumSum [256];
	pixel weightCumSum [256];
	int totalWeightCumSum = 0;
	cumSum[0] = histogram[0];
	weightCumSum[0] = histogram[0];
	for (i = 1; i < 256; ++i) {
		cumSum[i] = cumSum[i-1] + histogram[i];
		weightCumSum[i] = weightCumSum[i-1] + i * histogram[i];
		totalWeightCumSum += weightCumSum[i];
	}
	
	// find maximum Otsu variance
	int maxVariance = -65536; // there is probably a better way to get the smallest value
	int maxIndex = 0;
	int w0, w1, variance
	float u0, u1;
	for (i = 0; i < 256; ++i) {
		w0 = cumSum[i];		if (w0 == 0) continue;
		w1 = nPixels - w0;	if (w1 == 0) continue;
		
		u0 = weightCumSum[i] / w0;
		u1 = (totalWeightCumSum - weightCumSum[i]) / w1;
		
		variance = w0 * w1 * (int)pow(u0-u1, 2);
		if (variance > maxVariance) {
			maxVariance = variance;
			maxIndex = i;
		}
	}
	
	// return intensity value of maximum variance
	return maxIndex;
}