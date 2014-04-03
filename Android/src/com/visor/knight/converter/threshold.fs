#pragma version(1)
#pragma rs java_package_name(com.visor.knight)

rs_allocation in;
int32_t threshold;
uchar4 color;
bool softEdges;

uchar4 __attribute__((kernel)) root(const uint32_t x, const uint32_t y) {
	
	const uchar4 g = rsGetElementAt_uchar4(in, x, y);
			
	if (g.x > threshold) {
		if (softEdges) {
			return color & g;
		} else {
			return color;
		}
	} else {
		return g;
	}
}
