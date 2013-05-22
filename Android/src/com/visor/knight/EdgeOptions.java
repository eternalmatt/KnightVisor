package com.visor.knight;

public class EdgeOptions {
    
    private int threshold;
    private int color;
    private boolean medianFiltering;
    private boolean grayscaleOnly;
    private boolean automaticThreshold;
    private boolean logarithmicTransform;
    private boolean softEdges;
    
    public int getThreshold() {
        return threshold;
    }
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public boolean isMedianFiltering() {
        return medianFiltering;
    }
    public void setMedianFiltering(boolean medianFiltering) {
        this.medianFiltering = medianFiltering;
    }
    public boolean isGrayscaleOnly() {
        return grayscaleOnly;
    }
    public void setGrayscaleOnly(boolean grayscaleOnly) {
        this.grayscaleOnly = grayscaleOnly;
    }
    public boolean isAutomaticThreshold() {
        return automaticThreshold;
    }
    public void setAutomaticThreshold(boolean automaticThreshold) {
        this.automaticThreshold = automaticThreshold;
    }
    public boolean isLogarithmicTransform() {
        return logarithmicTransform;
    }
    public void setLogarithmicTransform(boolean logarithmicTransform) {
        this.logarithmicTransform = logarithmicTransform;
    }
    public boolean isSoftEdges() {
        return softEdges;
    }
    public void setSoftEdges(boolean softEdges) {
        this.softEdges = softEdges;
    }
}
