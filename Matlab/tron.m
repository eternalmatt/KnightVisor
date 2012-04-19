function [ greenImg ] = tron ( colorImg )
figure, imshow(colorImg), title('original color image')

% log transform
logMap = log(1:256) * 255 / log(256);
colorImg = uint8( logMap( uint16(colorImg)+1 ) );

% % histogram stretch
% dblGrayImg = double(rgb2gray(colorImg));
% minV = min( dblGrayImg(:) );
% maxV = max( dblGrayImg(:) );
% histoStretchMap = ((0:255)-minV) / (maxV-minV) * 255;
% colorImg = uint8( histoStretchMap( uint16(colorImg)+1 ) );

% use sobel edge detection on f and apply thresholding.
grayImg = rgb2gray(colorImg);
edges = sobel(grayImg);
edges = threshold(edges);

greenImg = colorImg;
greenImg(:,:,2) = greenImg(:,:,2) + uint8( double(edges) );

figure, imshow(colorImg), title('transformed color image')
figure, imshow(greenImg), title('final result')

