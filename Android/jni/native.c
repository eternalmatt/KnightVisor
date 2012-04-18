#include <edu_uncc_cci_KnightVisor_EdgeView.h>
#include <stdlib.h>

#include "dip-toolbox.h"

/* So I know this code is going to look complicated and gross,
   but it really isn't.  Let is sink in and read slowly.
   It basically goes like this:
        1. Get access to Java's memory.
        2. Create a local copy of input frame for performance reasons
        3. Do some processing and populate Java's memory.
 
    Scary stuff you'll see that are unecessary but help code:
        1. A typedef for pixel.
        2. A bunch of constants.
        3. A bunch of #define macros.
            a) colors so I can code in English.
            b) matrix positions so I can code and not worry about the underlying array.
        4. Bit shifting. Doing (x << 1) is the same thing as (x * 2).
 
    The last eight lines are all that is important to image processing.
 */

//refer to http://developer.android.com/reference/android/graphics/Color.html
// (or anywhere else that uses this same scheme. it's just hex of ARGB)
#define TRANSPARENT 0x00000000
#define WHITE       0xFFFFFFFF
#define BLACK       0xFF000000
#define GREEN       0xFF00FF00
#define BLUE        0xFFFF0000
#define RED         0xFF0000FF

#define nPixels  3000*2000
pixel f [nPixels]; //I really just want the biggest possible array to fit biggest posssible picture.

int threshold;// = 98;
int color     = GREEN;

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_setThresholdManually
    (JNIEnv *env, jobject thiz, jint thresh)
{
	// for automatic thresholding, let thresh be -1
    threshold = thresh >= 0 ? thresh : otsuThreshold(f, nPixels);
}

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_setColorSelected
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

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_nativeProcessing
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
    int p, integer;
    
    
    /* create a local copy so it is faster */
    for(p = 0; p < length; ++p)
    {
        integer = (int)fbytearray[p];    //cast to int
        f[p] = (pixel) integer + 128;    //add 128 so range is [0..255] and not [-128..127]
        
        /* the adding 128 might be unecessary if the math stays the same
         * everywhere (i.e. values don't matter until absolute final step)
         */
    }
    (*env)->ReleaseByteArrayElements(env, frame, fbytearray, JNI_COMMIT);
    
#define n11 (f[p-w-1])
#define n12 (f[p-w  ])
#define n13 (f[p-w+1])
#define n21 (f[p  -1])
#define n22 (f[p    ])
#define n23 (f[p  +1])
#define n31 (f[p+w-1])
#define n32 (f[p+w  ])
#define n33 (f[p+w+1])
    
    /* these definitions are so we can refer to a window like so:
     n11 n12 n13
     n21 n22 n23
     n31 n32 n33
     
     see, it looks pretty. just don't start using "n11" in other variable names...
     also, we have to keep this for loop structure the exact same. */
    
    
    
    int gx, gy, gm;
    for (p = start; p < stop; ++p)
    {
        //pixel pixels[] = { n11, n12, n13, n21, n22, n23, n31, n32, n33 };
        //sort(pixels, 9);
        
        gx = n13 + (n23 << 1) + n33 - (n11 + (n21 << 1) + n31);
        gy = n31 + (n32 << 1) + n33 - (n11 + (n12 << 1) + n13);
        
        gm = (gx + gy) /2;
        
        g[p] = gm > threshold ? color + gm*TRANSPARENT : TRANSPARENT;
        //g[p] = (f[p] << 16) | (f[p] << 8) | f[p];
    }
}

