%for now, hough takes a list of coordinates in x,y space
%and also the size of the space
%and returns the accumulator array in hough space
function [ accum ] = hough ( points, R, C )

tv = [-pi/2 : pi/200 : pi/2];  %thetaVector
rho0 = 0;                      %minimum rho
rho1 = floor(sqrt(R^2 + C^2)); %maximum rho (length of diagonal)

%accumulator is zeros(length of row, length of column)
accum = zeros(numel(tv),rho1); 

for p = points  %foreach p in points
  x = p(1);
  y = p(2);
  rhov = x * cos(tv) + y * sin(tv);
  for j = 1:numel(tv)

    %if value in rhoVector is valid,
    %   increment the corresponding cell in accum
    if rhov(j) >= rho0 && rhov(j) <= rho1
      i = floor(rhov(j) - rho0 + 1);
      accum(i,j) = accum(i,j) + 1;
    end
  end
end


