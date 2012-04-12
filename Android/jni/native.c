#include <edu_uncc_cci_KnightVisor_EdgeView.h>
/*
 * Class:     edu_uncc_cci_KnightVisor_EdgeView
 * Method:    nativeProcessing
 * Signature: ([BIILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_nativeProcessing
  (JNIEnv *env, jobject obj, jbyteArray byteArray, jint width, jint height, jobject output)
{
  /* Get a pointer to the raw output buffer */
  jint *dest_buf = (jint*) ((*env)->GetDirectBufferAddress(env, output));

  /* Get a pointer to (probably a copy of) the input */
  jboolean frame_copy;
  jint *src_buf = (jint*) (*env)->GetByteArrayElements(env, byteArray, &frame_copy);

  int x, y, w = width, pos = width+1;
  int maxX = width-1, maxY = height-1;
  int sobelX, sobelY, sobelFinal;

  for(y=1; y<maxY; y++, pos+=2)
  {
    for(x=1; x<maxX; x++, pos++)
    {
      sobelX = src_buf[pos+w+1] - src_buf[pos+w-1]
                 + src_buf[pos+1] + src_buf[pos+1]
                 - src_buf[pos-1] - src_buf[pos-1]
                 + src_buf[pos-w+1] - src_buf[pos-w-1];
      sobelY = src_buf[pos+w+1] + src_buf[pos+w]
                 + src_buf[pos+w] + src_buf[pos+w-1]
                 - src_buf[pos-w+1] - src_buf[pos-w]
                 - src_buf[pos-w] - src_buf[pos-w-1];

      sobelFinal = (sobelX + sobelY) >> 1;
      if(sobelFinal < 48)  sobelFinal = 0;
      if(sobelFinal >= 48) sobelFinal = 255;

      dest_buf[pos] = (sobelFinal << 0)  |
                      (sobelFinal << 8)  |
                      (sobelFinal << 16) |
                      (sobelFinal << 24);
    }
  }
}
