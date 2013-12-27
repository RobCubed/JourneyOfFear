package game;

import org.newdawn.slick.Image;

public class ImageMask {
    
    private boolean[][] mask;
    
    public ImageMask(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        mask = new boolean[width][height];
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                mask[i][j] = image.getColor(j,i).getAlpha() == 255;
            }
        }
    }
    
    public String toString(boolean[][] array) {
        String s = "";
        for(int i=0;i<array.length;i++) {
            for(int j=0;j<array[i].length;j++) {
                s+=array[i][j]?"1":"0";
                //if(j<array[i].length-1) s+=" ";
            }
            s+="\n";
        }
        return s;
    }
    
    public String toString() {
        return toString(this.getMask());
    }
    
    public boolean intersects(ImageMask other) {
        return true;
    }

    public boolean intersects(Rectangle other) {
        return true;
    }
    
    public boolean[][] getMask() {
        return mask;
    }
}