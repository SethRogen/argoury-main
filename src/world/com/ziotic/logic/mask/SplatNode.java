package com.ziotic.logic.mask;

/**
 * @author Lazaro
 */
public class SplatNode {
    private Splat splat1;
    private Splat splat2;

    public SplatNode(Splat splat) {
        this(splat, null);
    }

    public SplatNode(Splat splat1, Splat splat2) {
        this.splat1 = splat1;
        this.splat2 = splat2;
    }

    public Splat getSplat1() {
        return splat1;
    }

    public Splat getSplat2() {
        return splat2;
    }
}
