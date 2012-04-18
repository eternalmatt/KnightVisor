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

const pixel logmap [256] = {0.0, 31.875, 50.52067971048686, 63.75, 74.01145802453468, 82.39567971048686, 89.48443814058614, 95.625, 101.04135942097372, 105.88645802453469, 110.26938284406387, 114.27067971048687, 117.95151601574732, 121.35943814058612, 124.53213773502154, 127.5, 130.2878780648546, 132.9163594209737, 135.40268949101429, 137.76145802453468, 140.00511785107298, 142.14438284406387, 144.1885373493173, 146.14567971048686, 148.02291604906935, 149.82651601574733, 151.56203913146055, 153.23443814058612, 154.84814421969136, 156.40713773502154, 157.91500739358167, 159.375, 160.7900625545507, 162.16287806485457, 163.4958961651208, 164.79135942097372, 166.05132602942277, 167.27768949101429, 168.47219572623416, 169.63645802453468, 170.77197014720144, 171.880117851073, 172.96218905612938, 174.01938284406384, 175.05281744550837, 176.0635373493173, 177.05251964722467, 178.0206797104869, 178.96887628117227, 179.89791604906935, 180.80855777534143, 181.70151601574733, 182.577464489202, 183.43703913146058, 184.28084086859855, 185.10943814058618, 185.92336920150117, 186.72314421969136, 187.50924719840873, 188.2821377350215, 189.042252634817, 189.79000739358165, 190.52579756155984, 191.25, 191.962974040282, 192.66506255455067, 193.3565929458415, 194.0378780648546, 194.70921705980416, 195.37089616512083, 196.02318943421173, 196.66635942097372, 197.30065781430054, 197.9263260294228, 198.5435957595562, 199.15268949101429, 199.75382098465002, 200.3471957262342, 200.9330113481452, 201.51145802453468, 202.08271884194744, 202.64697014720144, 203.20438187418324, 203.75511785107298, 204.29933608938927, 204.83718905612938, 205.36882393017822, 205.8943828440639, 206.41400311205393, 206.9278174455084, 207.43595415633342, 207.93853734931733, 208.43568710406853, 208.92751964722467, 209.41414751554896, 209.89567971048686, 210.3722218447147, 210.8438762811723, 211.31074226503756, 211.77291604906938, 212.2304910127135, 212.6835577753414, 213.1322043039651, 213.5765160157473, 214.01657587560763, 214.45246448920196, 214.88426019153655, 215.31203913146058, 215.73587535226451, 216.15584086859855, 216.5720057399096, 216.9844381405861, 217.39320442698414, 217.79836920150115, 218.19999537385198, 218.59814421969133, 218.99287543672102, 219.3842471984087, 219.77231620544072, 220.1571377350215, 220.53876568812774, 220.91725263481703, 221.29264985768825, 221.66500739358167, 222.03437407360406, 222.40079756155987, 222.7643243908628, 223.12499999999997, 223.4828687666162, 223.83797404028198, 224.19035817400624, 224.5400625545507, 224.88712763160044, 225.2315929458415, 225.57349715599523, 225.91287806485462, 226.2497726443668, 226.58421705980416, 226.9162466930618, 227.2458961651208, 227.57319935771156, 227.89818943421176, 228.22089885981114, 228.54135942097375, 228.85960224422604, 229.17565781430056, 229.4895559916591, 229.80132602942277, 230.11099658973143, 230.41859575955618, 230.72415106598692, 231.0276894910143, 231.3292374858283, 231.62882098465002, 231.92646541811632, 232.2221957262342, 232.51603637092063, 232.80801134814516, 233.09814419968885, 233.38645802453465, 233.67297548990342, 233.9577188419474, 234.24070991611563, 234.52197014720142, 234.80152057908535, 235.07938187418324, 235.35557432261047, 235.63011785107298, 235.90303203149463, 236.17433608938927, 236.44404891198803, 236.71218905612935, 236.9787747559206, 237.24382393017822, 237.5073541896555, 237.7693828440638, 238.02992690889553, 238.28900311205393, 238.5466279002982, 238.8028174455084, 239.0575876507772, 239.31095415633345, 239.56293234530386, 239.8135373493173, 240.06278405395744, 240.3106871040685, 240.55726090891847, 240.8025196472247, 241.0464772720467, 241.289147515549, 241.53054389363948, 241.77067971048686, 242.0095680629201, 242.24722184471472, 242.48365375076887, 242.71887628117224, 242.952901745172, 243.1857422650376, 243.4174097798288, 243.64791604906935, 243.87727265632836, 244.1054910127135, 244.33258236027748, 244.55855777534143, 244.78342817173612, 245.0072043039651, 245.22989677029102, 245.45151601574733, 245.67207233507816, 245.89157587560766, 246.11003664004153, 246.32746448920196, 246.5438691446986, 246.75926019153653, 246.97364708066405, 247.18703913146058, 247.3994455341678, 247.61087535226454, 247.8213375247874, 248.03084086859857, 248.23939408060187, 248.44700573990963, 248.65368430995974, 248.85943814058612, 249.06427547004307, 249.2682044269841, 249.47123303239792, 249.67336920150115, 249.8746207455901, 250.07499537385195, 250.27450069513682, 250.47314421969136, 250.6709333608552, 250.86787543672105, 251.06397767175935, 251.25924719840873, 251.45369105863205, 251.64731620544072, 251.84012950438637, 252.03213773502154, 252.22334759233004, 252.4137656881277, 252.60339855243427, 252.792252634817, 252.98033430570698, 253.16764985768828, 253.35420550676162, 253.54000739358167, 253.7250615846701, 253.90937407360403, 254.09295078218085, 254.27579756155984, 254.45792019338114, 254.63932439086278, 254.82001579987607, 255.0};

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
          pixel pixels[] = { n11, n12, n13, n21, n22, n23, n31, n32, n33 };
          n22 = median(pixels);
        }
    }
    
    if (automaticT)
    {
		// calculate histogram
		int histogram [256];
		for(f = pointer_start; f != pointer_stop; ++f)
			++histogram[n22];
		
		// calculate cumulative sum and weighted cumulative sum
		int cumSum [256];
		int weightCumSum [256];
		int totalWeightCumSum = 0;
		cumSum[0] = histogram[0];
		weightCumSum[0] = histogram[0];
		int i;
		for (i = 1; i < 256; ++i) {
			cumSum[i] = cumSum[i-1] + histogram[i];
			weightCumSum[i] = weightCumSum[i-1] + i * histogram[i];
			totalWeightCumSum += weightCumSum[i];
		}
		
		// find maximum Otsu variance
		int maxVariance = 0; // I am assuming I get no negative variances...
		int maxIndex = 0;
		int w0, w1, variance;
		float u0, u1;
		for (i = 0; i < 256; ++i) {
			w0 = cumSum[i];		if (w0 == 0) continue;
			w1 = nPixels - w0;	if (w1 == 0) continue;
			
			u0 = weightCumSum[i] / w0;
			u1 = (totalWeightCumSum - weightCumSum[i]) / w1;
			
			variance = w0 * w1 * (int)pow(u0-u1, 2);
			if (variance > maxVariance) {
				maxVariance = variance;
				maxIndex = i;
			}
		}

		threshold = maxIndex; // maxIndex is the threshold value
    }
	
	// log intensity transform
	for(f = pointer_start; f != pointer_stop; ++f) {
		n22 = logmap[n22];
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
            background = (a << 0) | (a << 8) | (a << 16) | 0xFF000000;
        }
        else
        {
            background = TRANSPARENT;
        }
        
		// this can be taken outside the for loop, but first make sure the code below is correct
		if (color == GREEN)
			g[f - pointer_start] = gm > threshold ? (((int)gm) << 2) & color | TRANSPARENT : background;
		else if (color == BLUE)
			g[f - pointer_start] = gm > threshold ? (((int)gm) << 4) & color | TRANSPARENT : background;
		else if (color == RED)
			g[f - pointer_start] = gm > threshold ? ((int)gm) & color | TRANSPARENT : background;
		else // if you decide to add more colors
			g[f - pointer_start] = gm > threshold ? color : background;
    }
}

pixel median(pixel a[])
{
	int i, k;
	pixel mins[6] = {a[0], 256, 256, 256, 256, 256};
	for (i = 1; i <= 4; ++i) {
		for (k = i - 1; a[i] < mins[k] && k >= 0; --k)
			mins[k+1] = mins[k];
		mins[k+1] = a[i];
	}
	for (i = 5; i < 9; ++i) {
		for(k = 4; a[i] < mins[k] && k >= 0; --k)
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
