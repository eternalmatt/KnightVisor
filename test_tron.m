clear all
close all
clc

img = imread('FRM00001.png');
%img = imread('Lenna.png');
img = uint8( double(img)/16 );

tron(img);
