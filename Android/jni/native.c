#include <native.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include "logmap.h"
#include "colors.h"

#define MAX_PIXELS (3000*2000)
typedef short pixel;
pixel image[MAX_PIXELS]; //I really just want the biggest possible array to fit biggest posssible picture.

pixel fastAndInaccurateMedian(const pixel a[9]);

bool logEnabled    = false;
bool medianEnabled = false;
bool grayscale     = false;
bool automaticT    = false;
bool softEdges     = false;
int threshold      = 42;
int color          = GREEN;

/* REALLY REALLY UGLY FUNCTION NAMES TO GET USER INPUT FROM THE JAVA GUI */

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setMedianFiltering
(JNIEnv *env, jobject obj, jboolean on)
{
    medianEnabled = on;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setThreshold
    (JNIEnv *env, jobject thiz, jint thresh)
{
    threshold = thresh;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setGrayscaleOnly
(JNIEnv *env, jobject obj, jboolean gray)
{
    grayscale = gray;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setAutomaticThreshold
(JNIEnv *env, jobject obj, jboolean autoT)
{
    automaticT = autoT;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setLogarithmicTransform
(JNIEnv *env, jobject obj, jboolean on)
{
    logEnabled = on;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setSoftEdges
(JNIEnv *env, jobject obj, jboolean on)
{
    softEdges = on;
}

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_setColor
    (JNIEnv *env, jobject thiz, jint userColor)
{
    /* For some reason the colors get mixed up and we
     * have to switch the red and blue channels  */
    const int redChannel   = 0x000000FF;
    const int greenChannel = 0x0000FF00;
    const int blueChannel  = 0x00FF0000;
    const int alphaChannel = 0xFF000000;
    
    color = (userColor & greenChannel)
          | ((userColor & redChannel)   << 16) //swap red
          | ((userColor & blueChannel)  >> 16) //with blue
          | alphaChannel;
}


/* the main image processing function that gets called by java */

JNIEXPORT void JNICALL Java_com_visor_knight_converter_NativeConverter_nativeProcessing
    (JNIEnv *env, jobject thiz, //these variables are in every JNI call
     
     /* the input byte[] camera frame, width, height, and output int[] buffer */
     jbyteArray frame, jint width, jint height, jobject buffer)
{
	jbyte *fbytearray = (jbyte*)(*env)->GetByteArrayElements(env, frame, 0);
	jint  *g          = (jint*)((*env)->GetDirectBufferAddress(env, buffer));
    
    const int w      = width;               //"width" is too long of a word
    const int length = width * height;      //optimize out the multiply
    const int start  = w + 1;               //we can't operate on an entire image
    const int stop   = length - w - 1;      //we can't operate on an entire image.
    pixel *pointer_start = image;           //used in the for loops
    pixel *pointer_stop  = image + stop;    //used in the for loops
    pixel *f;
	int i;
    
    /* create a local copy so it is faster */
    for (i = start; i < stop; ++i)
    {
        image[i] = fbytearray[i] & 0xFF;
    }
    (*env)->ReleaseByteArrayElements(env, frame, fbytearray, JNI_ABORT);
    

#define n11 (f[0-w-1])
#define n12 (f[0-w  ])
#define n13 (f[0-w+1])
#define n21 (f[   -1])
#define n22 (f[    0])
#define n23 (f[   +1])
#define n31 (f[  w-1])
#define n32 (f[  w  ])
#define n33 (f[  w+1])
/* 
these definitions are so we can refer to a window like so:
         n11 n12 n13
         n21 n22 n23
         n31 n32 n33
*/
      
    if (medianEnabled)
    {
        for(f = pointer_start; f != pointer_stop; ++f)
        {
          pixel pixels[9] = { n11, n12, n13, n21, n22, n23, n31, n32, n33 };
          n22 = fastAndInaccurateMedian(pixels);
        }
    }

    
    /* log intensity transform */
    if (logEnabled)
    {
        for(f = pointer_start; f != pointer_stop; ++f) {
            n22 = logmap[n22];
        }
    }
	
    /* sobel edge detection */
    int gx, gy, gm, background, edgeColor;
    for(f = pointer_start, i = start; f != pointer_stop; ++f, ++i)
    {
        pixel f11 = n11, f12 = n12, f13 = n13;
        pixel f21 = n21, f22 = n22, f23 = n23;
        pixel f31 = n31, f32 = n32, f33 = n33;
        
        gx = f13 + (f23 << 1) + f33 - (f11 + (f21 << 1) + f31);
        gy = f31 + (f32 << 1) + f33 - (f11 + (f12 << 1) + f13);
        
        gm = (gx + gy) /2;

        if (grayscale)
        {
            int a = f22;
            background = (a << 0) | (a << 8) | (a << 16) | 0xFF000000;
        }
        else
        {
            background = TRANSPARENT;
        }
        
        edgeColor = softEdges ? (gm | (gm<<8) | (gm<<16)) & color : color;
        
        g[i] = gm > threshold ? edgeColor : background;
    }
    
}

pixel fastAndInaccurateMedian(const pixel f[9])
{
#define MAXHIST (8)
    int i, sum;

/* you can either do it the hard way,
 * or the easy way. Easy way is innaccurate,
 * but hella fast. */
#if MAXHIST > 8
    int hist[MAXHIST];
    for(i = 0; i < MAXHIST; i++) 
        hist[i] = 0;
#else
    int hist[MAXHIST] = {0,0,0,0,0,0,0,0}; 
#endif
    
    
    for(i = 0; i < 9; i++)
    {
        int p = f[i] / 32;
        hist[ p < MAXHIST ? p : MAXHIST - 1]++;
    }
    
    sum = hist[0];
    for(i = 1; sum < 5; i++)
        sum += hist[i];
    
    return f[5];
#undef MAXHIST
}

/*
#define m11 (f[p-w-1] * k[1])
#define m12 (f[p-w  ] * k[2])
#define m13 (f[p-w+1] * k[3])
#define m21 (f[p  -1] * k[4])
#define m22 (f[p    ] * k[5])
#define m23 (f[p  +1] * k[6])
#define m31 (f[p+w-1] * k[7])
#define m32 (f[p+w-1] * k[8])
#define m33 (f[p+w-1] * k[9])

#define M_SUM ( (((m11+m12)+(m13+m21)) + ((m22+m23)+(m31+m32))) + m33 )
#define CONVOLUTION (M_SUM / 9)

void imfilter(int *f, float *k, int start, int stop, int w)
{
	int g[nPixels], p;
    
	for(p=start; p<stop; ++p)
        g[p] = CONVOLUTION;
    
	for(p=start; p<stop; ++p)
		f[p] = g[p];
}

void mean(int *f, int start, int stop, int w)
{
	float k[] = {0.03125, 0.09375, 0.03125, 0.09375, 0.5, 0.09375, 0.03125, 0.09375, 0.03125};
	imfilter(f, k, start, stop, w);
}
*/

