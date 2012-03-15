function [ g ] = tron ( f )

edges = sobel(f);
edges = threshold(edges);

[R,C] = size(f);
g = uint8(zeros(R,C,3));

for r = 1:R
for c = 1:C
  g(r,c,:) = f(r,c);
  
  if (edges(r,c) == 1)
    g(r,c,2) = 255;
  end

end
end

figure;
imshow(g);
