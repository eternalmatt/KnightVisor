#include <edu_uncc_cci_KnightVisor_EdgeView.h>
#include <stdlib.h>
#include <stdbool.h>

/* So I know this code is going to look complicated and gross,
   but it really isn't.  Let it sink in and read slowly.
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

typedef short pixel;
pixel image[3000 * 2000]; //I really just want the biggest possible array to fit biggest posssible picture.

void imfilter(int*,int*,int,int,int);
pixel median(pixel a[]);

bool medianEnabled = false;
bool grayscale     = false;
bool automaticT    = false;
int threshold      = 100;
int color          = GREEN;

/* REALLY REALLY UGLY FUNCTION NAMES TO GET USER INPUT FROM THE JAVA GUI */

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_setMedianFiltering
    (JNIEnv *env, jobject obj, jboolean med)
{
  medianEnabled = med;
}

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_setThresholdManually
    (JNIEnv *env, jobject obj, jint thresh)
{
    threshold = thresh;
}

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_grayscaleOnly
(JNIEnv *env, jobject obj, jboolean gray)
{
    grayscale = gray;
}

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_automaticThresholding
(JNIEnv *env, jobject obj, jboolean autoT)
{
    automaticT = autoT;
}

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_setColorSelected
    (JNIEnv *env, jobject obj, jint userColor)
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

JNIEXPORT void JNICALL Java_edu_uncc_cci_KnightVisor_EdgeView_nativeProcessing
    (JNIEnv *env, jobject obj, //these variables are in every JNI call
     
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

    
    /* create a local copy so it is faster */
    int p;
    for (p = start; p < stop; ++p)
    {
        image[p] = fbytearray[p] & 0x000000FF; /* only copy 0..255 */
    }
    (*env)->ReleaseByteArrayElements(env, frame, fbytearray, JNI_COMMIT);
    

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
          pixel pixels[] = { n11, n12, n13, n21, n22, n23, n31, n32, n33 };
          f[0] = median(pixels);
        }
    }
    
    if (automaticT)
    {
        /* do automatic thresholding. and set "threshold" */
    }
    
    int gx, gy, gm, background;
    for(f = pointer_start; f != pointer_stop; ++f)
    {
        gx = n13 + (n23 << 1) + n33 - (n11 + (n21 << 1) + n31);
        gy = n31 + (n32 << 1) + n33 - (n11 + (n12 << 1) + n13);
        
        gm = gx + gy;
        
        if (grayscale)
        {
            const int a = f[0];
            background = (a << 0) | ( a << 8) | (a << 16) | 0xFF000000;
        }
        else
        {
            background = TRANSPARENT;
        }
        
        g[f - pointer_start] = gm > threshold ? color : background;
    }
}

pixel median(pixel a[])
{
  int i, k;
  pixel mins[6] = {a[0], 256, 256, 256, 256, 256};
  for (i = 1; i < 9; ++i) {
    for(k=4; a[i] < mins[k] && k >= 0; --k)
        mins[k+1] = mins[k];
      
    mins[k+1] = a[i];
  }    
  
  return mins[4];
}


#define m11 (f[p-w-1] * k[1])
#define m12 (f[p-w  ] * k[2])
#define m13 (f[p-w+1] * k[3])
#define m21 (f[p  -1] * k[4])
#define m22 (f[p    ] * k[5])
#define m23 (f[p  +1] * k[6])
#define m31 (f[p+w-1] * k[7])
#define m32 (f[p+w-1] * k[8])
#define m33 (f[p+w-1] * k[9])

#define M_SUM (m11 + m12 + m13 + m21 + m22 + m23 + m31 + m32 + m33)
#define CONVOLUTION (M_SUM / 9)

/* I have no idea if this works.
 * In theory, it is a generic imfilter implementation
 * for a 3x3 kernel (flattened in a 1x9 array) */
void imfilter(int *f, int *k, int start, int stop, int w)
{
	int g[2000*3000], p;
    
	for(p=start; p<stop; ++p)
        g[p] = CONVOLUTION;
    
	for(p=start; p<stop; ++p)
		f[p] = g[p];
}

