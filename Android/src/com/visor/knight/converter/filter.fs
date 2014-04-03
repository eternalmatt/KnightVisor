#pragma version(1)
#pragma rs java_package_name(com.visor.knight)

typedef int32_t  number;
typedef uchar4   point;
typedef int32_t  pixel;

rs_allocation in;

number width = 0;
number height = 0;
number threshold = 42;
point color = { 0, 255, 0, 255 };
bool soft = false;

static inline bool outOfBounds(const number x, const number y) {
	const number b = 1;
	return x-b <= 0 || y-b <= 0 || x+b >= width || y+b >= height;
}

static inline pixel getElementAt(const number x, const number y) {
	point p = rsGetElementAt_uchar4(in, x, y);
	return ((p.x + p.y + p.z ) / 3) & 0xFF; //average and restrict to 255
}

point __attribute__((kernel)) sobel(const uint32_t x, const uint32_t y) {

	if (outOfBounds(x,y)){
		return rsGetElementAt_uchar4(in, x, y);
	} 

	const pixel
		f11 = getElementAt(x-1,y-1)
	  , f12 = getElementAt(x-1,y  )
	  , f13 = getElementAt(x-1,y+1)
	  , f21 = getElementAt(x  ,y-1)
	  , f22 = getElementAt(x  ,y  )
	  , f23 = getElementAt(x  ,y+1)
	  , f31 = getElementAt(x+1,y-1)
	  , f32 = getElementAt(x+1,y  )
	  , f33 = getElementAt(x+1,y+1)
	  ;

	const pixel
	    gx = (f13 + (f23 << 1) + f33) - (f11 + (f21 << 1) + f31)
      , gy = (f11 + (f12 << 1) + f13) - (f31 + (f32 << 1) + f33)
	  , gm = (gx + gy) / 2
	  ;
    
    if (gm > threshold) {
    	point p = color & (point){ gm, gm, gm, 255};
    	return p;
    } else {
    	return rsGetElementAt_uchar4(in, x, y);
    }
}

point __attribute__((kernel)) median(uint32_t x, uint32_t y) {
	if (outOfBounds(x,y)) {
		return rsGetElementAt_uchar4(in, x, y);
	}
	#define MAXHIST (8)
	const pixel
		f11 = getElementAt(x-1,y-1)
	  , f12 = getElementAt(x-1,y  )
	  , f13 = getElementAt(x-1,y+1)
	  , f21 = getElementAt(x  ,y-1)
	  , f22 = getElementAt(x  ,y  )
	  , f23 = getElementAt(x  ,y+1)
	  , f31 = getElementAt(x+1,y-1)
	  , f32 = getElementAt(x+1,y  )
	  , f33 = getElementAt(x+1,y+1)
	  ;
	
	const pixel f[9] = { f11, f12, f13, f21, f22, f23, f31, f32, f33 };
	int i, sum, hist[MAXHIST] = {0,0,0,0,0,0,0,0}; 
	for(i = 0; i < 9; i++)
	{
		int p = f[i] / 32;
		hist[ p < MAXHIST ? p : MAXHIST - 1]++;
	}
	
	sum = hist[0];
	for(i = 1; sum < 5; i++){
		sum += hist[i];
	}
	
	uint p = f[i];
	return convert_uchar4((uint4){p,p,p,0});
}
