/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_visor_knight_converter_NativeConverter */

#ifndef _Included_com_visor_knight_converter_NativeConverter
#define _Included_com_visor_knight_converter_NativeConverter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    nativeProcessing
 * Signature: ([BIILjava/nio/IntBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_nativeProcessing
  (JNIEnv *, jobject, jbyteArray, jint, jint, jobject);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    setThresholdManually
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setThreshold
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    setColorSelected
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setColor
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    setMedianFiltering
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setMedianFiltering
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    grayscaleOnly
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setGrayscaleOnly
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    automaticThresholding
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setAutomaticThreshold
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    logarithmicTransform
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setLogarithmicTransform
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_visor_knight_converter_NativeConverter
 * Method:    setSoftEdges
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setSoftEdges
  (JNIEnv *, jobject, jboolean);

#ifdef __cplusplus
}
#endif
#endif
