function [ g ] = tron ( f )

% use sobel edge detection on f and apply thresholding.
edges = sobel(f);
edges = threshold(edges);

% g will be a rgb image with f's dimensions
[R,C] = size(f);
g = uint8(zeros(R,C,3));

for r = 1:R
for c = 1:C

  % g's red/green/blue channels = f's gray intensity
  g(r,c,:) = f(r,c);
  
  % if an edge exists, set green to maximum
  if edges(r,c)
    g(r,c,2) = 255;
  end

end
end

imshow(g);
