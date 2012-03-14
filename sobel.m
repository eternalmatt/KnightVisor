function [ g ] = sobel( f )

kernel = [-1 -2 -1; 0 0 0; 1 2 1];

f = double(f);
gx = imfilter(f, kernel);
gy = imfilter(f, kernel');
gm = uint8(sqrt( gx.^2 + gy.^2 ));

imshow(gm);
g = gm;

