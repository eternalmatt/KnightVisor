package edu.uncc.cci.KnightVisor;

public class Toolbox 
{
    private Toolbox(){}
    public interface SingleOperation {
        public int it(int a);
    }
    public interface DoubleOperation {
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
    
    public static int[][] transform(final int[][] f, final int[][] g, DoubleOperation op)
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
    
    public static int[][] transform(final int[][] f, SingleOperation op)
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
}
