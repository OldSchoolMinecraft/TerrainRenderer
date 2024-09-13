package me.modman.tr.util;

public class PixelColorRedux
{
    // Constants for color conversion
    private static final float COLOR_SCALE = 1.0f / 255.0f;

    // Color components
    private boolean alphaComposite;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    // Constructors
    public PixelColorRedux()
    {
        this(true);  // Default to alpha compositing enabled
    }

    public PixelColorRedux(boolean alphaComposite)
    {
        this.alphaComposite = alphaComposite;
        this.clear();
    }

    // Clears the color (resets to fully transparent black)
    public void clear()
    {
        this.red = this.green = this.blue = this.alpha = 0.0F;
    }

    // Composite color with ARGB value
    public void composite(int argb)
    {
        this.composite(argb, 1.0F);
    }

    // Composite color with ARGB value and a light factor
    public void composite(int argb, float light)
    {
        float a = (argb >> 24 & 0xFF) * COLOR_SCALE;
        float r = (argb >> 16 & 0xFF) * COLOR_SCALE * light;
        float g = (argb >> 8 & 0xFF) * COLOR_SCALE * light;
        float b = (argb & 0xFF) * COLOR_SCALE * light;

        if (this.alphaComposite)
        {
            this.red += (r - this.red) * a;
            this.green += (g - this.green) * a;
            this.blue += (b - this.blue) * a;
            this.alpha += (1.0F - this.alpha) * a;
        } else {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.alpha = a;
        }
    }

    // Composite color with specific alpha and RGB values
    public void composite(float alpha, int rgb, float light)
    {
        float r = (rgb >> 16 & 0xFF) * COLOR_SCALE * light;
        float g = (rgb >> 8 & 0xFF) * COLOR_SCALE * light;
        float b = (rgb & 0xFF) * COLOR_SCALE * light;

        if (this.alphaComposite)
        {
            this.red += (r - this.red) * alpha;
            this.green += (g - this.green) * alpha;
            this.blue += (b - this.blue) * alpha;
            this.alpha += (1.0F - this.alpha) * alpha;
        } else {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.alpha = alpha;
        }
    }

    // Composite color with individual components
    public void composite(float alpha, float red, float green, float blue)
    {
        if (this.alphaComposite)
        {
            this.red += (red - this.red) * alpha;
            this.green += (green - this.green) * alpha;
            this.blue += (blue - this.blue) * alpha;
            this.alpha += (1.0F - this.alpha) * alpha;
        } else {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    // Composite color with individual components and light factor
    public void composite(float alpha, float red, float green, float blue, float light)
    {
        red *= light;
        green *= light;
        blue *= light;

        if (this.alphaComposite)
        {
            this.red += (red - this.red) * alpha;
            this.green += (green - this.green) * alpha;
            this.blue += (blue - this.blue) * alpha;
            this.alpha += (1.0F - this.alpha) * alpha;
        } else {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    // Getters for color components
    public float getRed()
    {
        return red;
    }

    public float getGreen()
    {
        return green;
    }

    public float getBlue()
    {
        return blue;
    }

    public float getAlpha()
    {
        return alpha;
    }

    // Converts float color components back to an int (ARGB format)
    public int toARGB()
    {
        int a = Math.round(alpha * 255.0F);
        int r = Math.round(red * 255.0F);
        int g = Math.round(green * 255.0F);
        int b = Math.round(blue * 255.0F);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
