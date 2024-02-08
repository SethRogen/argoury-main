package com.runescape.content.chardesign;

public class DefaultDesign {

    protected DefaultSubDesign[] subDesigns;
    protected int[][] colours;

    public DefaultDesign(DefaultSubDesign[] subDesigns, int[][] colours) {
        this.subDesigns = subDesigns;
        this.colours = colours;
    }

    public static class DefaultSubDesign {

        protected int[][] looks;

        public DefaultSubDesign(int[][] looks) {
            this.looks = looks;
        }

    }

}
