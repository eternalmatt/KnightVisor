#include <edu_uncc_cci_KnightVisor_EdgeView.h>
/*
 * Class:     edu_uncc_cci_KnightVisor_EdgeView
 * Method:    nativeProcessing
 * Signature: ([BIILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_nativeProcessing
    (JNIEnv *env, jobject this,
     jbyteArray frame, jint width, jint height, jobject buffer)
{
	jint  *g = (jint*)((*env)->GetDirectBufferAddress(env, buffer));
	jbyte *f = (jbyte*)(*env)->GetByteArrayElements(env, frame, 0);
    const int w = width, maxx = width - 1, maxy = height - 1, length = width * height - width - 1;
    int y;
    
#define n11 (f[p-w-1])
#define n12 (f[p-w  ])
#define n13 (f[p-w+1])
#define n21 (f[p  -1])
#define n22 (f[p    ])
#define n23 (f[p  +1])
#define n31 (f[p+w-1])
#define n32 (f[p+w  ])
#define n33 (f[p+w+1])
    
#define WHITE       0xFFFFFFFF
#define TRANSPARENT 0x00000000
    
    /* these definitions are so we can refer to a window like so:
     n11 n12 n13
     n21 n22 n23
     n31 n32 n33
     
     see, it looks pretty. just don't start using "n11" in other variable names...
     */
    int p;
    #pragma omp parallel for default(shared) private(p)
    for (p = width + 1; p < length; p++)
    {
        int px, py, ps;
        px = n13 + (n23 << 1) + n33 - (n11 + (n21 << 1) + n31);
        //previously what I was using:
        //px = 0 - f[p-w-1] + f[p-w+1] - (f[p-1]<<1) + (f[p+1]<<1) - f[p+w-1] + f[p+w+1];
        
        py =  n31 + (n32 << 1) + n33 - (n11 + (n12 << 1) + n13);
        //py = f[p-w-1] + (f[p-w]<<1) + f[p-w+1] - ( f[p+w-1] + (f[p+w]<<1) + f[p+w+1] );
        
        if(px<0) px=-px; 
        if(py<0) py=-py;
        ps=px+py; 
        //if(ps>95) ps=255; if(ps<=95) ps=0;
        //dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
        g[p] = ps > 95 ? WHITE : TRANSPARENT; 
    }
    /*
    #pragma omp parallel for default(shared) private(y)
	for(y=1; y<maxy; y++)//, p+=2)
	{
        const int row = y * width;
        int x, px, py, ps;
	
        for(x=1; x<maxx; x++)//, p++)
		{
            const int p = row + x;
            
            px = n13 + (n23 << 1) + n33 - (n11 + (n21 << 1) + n31);
            //previously what I was using:
            //px = 0 - f[p-w-1] + f[p-w+1] - (f[p-1]<<1) + (f[p+1]<<1) - f[p+w-1] + f[p+w+1];
			
            py =  n31 + (n32 << 1) + n33 - (n11 + (n12 << 1) + n13);
            //py = f[p-w-1] + (f[p-w]<<1) + f[p-w+1] - ( f[p+w-1] + (f[p+w]<<1) + f[p+w+1] );
            
        	if(px<0) px=-px; 
            if(py<0) py=-py;
			ps=px+py; 
            //if(ps>95) ps=255; if(ps<=95) ps=0;
			//dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
            g[p] = ps > 95 ? WHITE : TRANSPARENT; 
		}
	}
    */
    (*env)->ReleaseByteArrayElements(env, frame, f, JNI_COMMIT);
}
