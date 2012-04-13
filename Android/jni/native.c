#include <edu_uncc_cci_KnightVisor_EdgeView.h>
/*
 * Class:     edu_uncc_cci_KnightVisor_EdgeView
 * Method:    nativeProcessing
 * Signature: ([BIILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_nativeProcessing(
                                                                                JNIEnv *env, jobject this,
                                                                                jbyteArray frame, jint width, jint height, jobject diff)
{
	jint *dbuf = (jint*)((*env)->GetDirectBufferAddress(env, diff));
	jbyte *fbuf = (jbyte*)(*env)->GetByteArrayElements(env, frame, 0);
	int x, y, maxx=width-1, maxy=height-1, p=width+1, px, py, ps;
    
	for(y=1; y<maxy; y++, p+=2)
	{
		for(x=1; x<maxx; x++, p++)
		{
			px = fbuf[p+width+1]-fbuf[p+width-1]+fbuf[p+1]+fbuf[p+1]-fbuf[p-1]-fbuf[p-1]+fbuf[p-width+1]-fbuf[p-width-1];
			py = fbuf[p-width-1]+fbuf[p-width]+fbuf[p-width]+fbuf[p-width+1]-fbuf[p+width-1]-fbuf[p+width]-fbuf[p+width]-fbuf[p+width+1];
			if(px<0) px=-px; if(py<0) py=-py;
			ps=px+py; if(ps>95) ps=255; if(ps<=95) ps=0;
			dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
            //			dbuf[p] = (ps<<24)|0x00FFFFFF;
		}
	}
    
    (*env)->ReleaseByteArrayElements(env, frame, fbuf, JNI_COMMIT);
}
