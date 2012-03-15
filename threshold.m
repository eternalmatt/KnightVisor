function [ g ] = threshold ( f )

t = (max(f(:)) + min(f(:)) ) / 2;
diff = 99999;
epsilon = 0.1;

while diff > epsilon
    above = f(f <  t);      %values above guess
    below = f(f >= t);      %values below guess
    u1 = mean(above);       %average above
    u2 = mean(below);       %average below
    v = (u1 + u2) / 2;      %average of the averages
    diff = abs(t - v);      %difference between current threshold and next
    t = v;                  %update threshold
end

g = f > t;

