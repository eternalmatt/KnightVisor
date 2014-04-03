#pragma version(1)
#pragma rs java_package_name(com.visor.knight)

rs_allocation in;

uchar4 __attribute__((kernel)) root(const uint32_t x, const uint32_t y) {
	const uchar4 p = rsGetElementAt_uchar4(in, x, y);
	return ((p.x + p.y + p.z ) / 3) & 0xFF; //average and restrict to 255
}
