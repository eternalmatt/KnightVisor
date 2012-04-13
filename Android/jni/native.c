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
	jbyte *f = (jbyte*)(*env)->GetByteArrayElements(env, frame, 0);
	jint  *g = (jint*)((*env)->GetDirectBufferAddress(env, buffer));
    const int w = width, maxx = width - 1, maxy = height - 1, length = width * height - width - 1;
    
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
     
     see, it looks pretty. just don't start using "n11" in other variable names...*/
    int p;
    #pragma omp parallel for default(shared) private(p) schedule(static)
    for (p = width + 1; p < length; p++)
    {
        int px = n13 + (n23 << 1) + n33 - (n11 + (n21 << 1) + n31);
        int py = n31 + (n32 << 1) + n33 - (n11 + (n12 << 1) + n13);
        
        if(px<0) px=-px; 
        if(py<0) py=-py;
        int ps = px+py;
        g[p] = ps > 95 ? WHITE : TRANSPARENT; 
    }
    
    (*env)->ReleaseByteArrayElements(env, frame, f, JNI_COMMIT);
}
