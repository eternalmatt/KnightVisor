package edu.uncc.cci.KnightVisor;

public class Toolbox 
{
    public static interface UnaryOperation {
        public int it(int a);
    }
    public static interface BinaryOperation {
        public int it(int a,int b);
    }
    
    /* i think I have trust issues.... */
    public static final float smoothSize = 7.0f;
    public static final float smooth = 1.0f / (smoothSize * smoothSize);
    
    public static int[][] smooth(int[][] f)
    {
        final int length = (int)smoothSize;
        final int R = f.length - length;
        final int C = f[0].length - length;
        int r, c, i, j;
        
        int [][] g = new int[R+length][C+length];
        int sum;
        
        /* for each row,col in f */
        for(r=0; r < R; r++)
        for(c=0; c < C; c++)
        {
            sum = 0;
         
            /* sum up a sub-matrix of f */
            for(i=0; i < length; i++)
            for(j=0; j < length; j++)
            {
                sum = sum + f[r+i][c+j];
            }
        
            /* multiply by a factor to fit back to normal range */
            g[r][c] = (int) (smooth * sum);
        }
        return g;
    }
    
    
    public static int[][] imfilter(final int[][] f, final byte[][] kernel)
    {
        final int length = kernel.length;
        final int R = f.length - length;
        final int C = f[0].length - length;
        final int[][] g = new int[R+length][C+length];
        int r, c, i, j, cell;
        
        /* for each row,col in f */
        for(r=0; r < R; r++)
        for(c=0; c < C; c++)
        {
            cell = 0;
         
            /* perform matrix dot-multiplication and get summation of that matrix */
            for(i=0; i < length; i++)
            for(j=0; j < length; j++)
            //for(k=0; k < length; k++)
            {
                cell = cell + f[r + i][c + j] * kernel[i][j];
            }
        
            g[r][c] = cell;
        }
        return g;
    }
    
    public static int[][] transform(final int[][] f, final int[][] g, BinaryOperation op)
    {
        final int R = f.length;
        final int C = f[0].length;
        final int[][] h = new int[R][C];
        
        int r,c;
        for(r=0; r < R; r++)
        for(c=0; c < C; c++)
            h[r][c] = op.it(f[r][c], g[r][c]);
        
        return h;
    }
    
    public static int[][] transform(final int[][] f, UnaryOperation op)
    {
        final int R = f.length;
        final int C = f[0].length;
        final int[][] h = new int[R][C];
        
        int r,c;
        for(r=0; r < R; r++)
        for(c=0; c < C; c++)
            h[r][c] = op.it(f[r][c]);
        
        return h;
    }
    
    // this will do a transformation on f rather than on a new allocation
    public static void intensityTransform(final int[][] f, UnaryOperation op)
    {
        final int R = f.length;
        final int C = f[0].length;
        
        int r, c;
        for(r = 0; r < R; r++)
        	for(c = 0; c < C; c++)
        		f[r][c] = op.it(f[r][c]);
    }
    
    public static int threshold(final int[][] f)
    {
        final int R = f.length;
        final int C = f[0].length;
    	final int nPixels = R * C;
    	
    	// calculate histogram, 
    	final int[] histogram = new int[256];
    	for (int r = 0; r < R; r++)
    		for (int c = 0; c < C; c++)
    			histogram[f[r][c]]++;
    	
    	// calculate cumulative sum and weighted cumulative sum
    	final int[] cumSum = new int[256];
    	final int[] weightCumSum = new int[256];
    	int totalWeightCumSum = 0;
    	cumSum[0] = histogram[0];
    	weightCumSum[0] = histogram[0];
    	for (int i = 1; i < 256; i++) {
    		cumSum[i] = cumSum[i-1] + histogram[i];
    		weightCumSum[i] = weightCumSum[i-1] + i * histogram[i];
    		totalWeightCumSum += weightCumSum[i];
    	}
    	
    	// find maximum Otsu variance
    	int maxVariance = Integer.MIN_VALUE;
    	int maxIndex = 0;
    	for (int i = 0; i < 256; i++) {
    		int w0 = cumSum[i];		if (w0 == 0) continue;
    		int w1 = nPixels - w0;	if (w1 == 0) continue;
    		
    		double u0 = weightCumSum[i] / w0;
    		double u1 = (totalWeightCumSum - weightCumSum[i]) / w1;
    		
    		int variance = w0 * w1 * (int)Math.pow(u0-u1, 2);
    		if (variance > maxVariance) {
    			maxVariance = variance;
    			maxIndex = i;
    		}
    	}
    	
    	// return intensity value of maximum variance
    	return maxIndex;
    }
}
