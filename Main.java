import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Main {

    private static void processImages(File[] listOfFiles, Map<Integer, BufferedImage> imagesByColor, Octree octree)
            throws IOException {
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                BufferedImage image = ImageIO.read(file);
                Color imageColor = getAverageColor(image);
                octree.insert(imageColor);
                imagesByColor.put(imageColor.hashCode(), image);
            }
        }
    }

    private static Color getAverageColor(BufferedImage image) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0, nonTransparentPixelsCount = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int color = image.getRGB(x, y);
                int alpha = (color >> 24) & 0xff;
                if (alpha == 0){
                    continue;
                }
                nonTransparentPixelsCount += 1;
                redTotal += (color >> 16) & 0xff;
                greenTotal += (color >> 8) & 0xff;
                blueTotal += (color) & 0xff;
            }
        }
        redTotal /= nonTransparentPixelsCount;
        greenTotal /= nonTransparentPixelsCount;
        blueTotal /= nonTransparentPixelsCount;
        return new Color(redTotal, greenTotal, blueTotal);
    }

    private static Color getColorXY(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        return new Color((color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);
    }

    private static float brightness(Color color) {
        return (Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2]);
    }

    private static Point3D nearestNeighborInOctree(Octree colorsOctree, Color color) {
        return colorsOctree.nearestNeighbor(new Point3D(color.getRed(), color.getGreen(), color.getBlue()));
    }

    private static Color pointToColor(Point3D p) {
        return new Color(p.getX(), p.getY(), p.getZ());
    }

    private static Mode getModeFromUser() {
        System.out.println("Choose a mode:");
        for (int i = 0; i < Mode.values().length; i++) {
            System.out.println(i + 1 + ". " + Mode.values()[i].getDescription());
        }

        System.out.print("Enter your choice: ");
        Scanner scanner = new Scanner(System.in);
        int modeCode = scanner.nextInt();
        scanner.close(); 

        if (modeCode < 1 || modeCode > Mode.values().length) {
            System.out.println("Invalid choice!");
            return null;
        } else {
            return Mode.values()[modeCode - 1];
        }
    }

    private static String getFilePathFromUser() {
        Frame frame = new Frame();
        FileDialog fileDialog = new FileDialog(frame, "Select a file", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String filePath = fileDialog.getDirectory() + fileDialog.getFile();
        frame.dispose();
        return filePath;
    }

    private static Pair<BufferedImage,Integer> recreateImage(Octree colorsOctree, Map<Integer, BufferedImage> imagesByColor, BufferedImage image,  Mode mode){
        int scale = mode.getScale();
        BufferedImage newImage = new BufferedImage(image.getWidth() * scale, image.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = newImage.createGraphics();

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
        int nsprites = (int) image.getWidth() * image.getHeight() * mode.getNSpritesModifier();
        Random random = new Random();
        int spriteCounter = 0;
        for (int i = 0; i < nsprites; i += 1) {
            int x = random.nextInt(newImage.getWidth()), y = random.nextInt(newImage.getHeight());
            Color pixelColor = getColorXY(image, x/scale, y/scale);
            float pixelBrightness = brightness(pixelColor);
            Point3D point = nearestNeighborInOctree(colorsOctree, pixelColor);
            Color colorOfbestImage = pointToColor(point);
            BufferedImage bestImage = imagesByColor.get(colorOfbestImage.hashCode());
            if (i % 6500 == 0)
                System.out.println(String.format("%d/%d", i, nsprites));
            if (bestImage == null)
                continue;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pixelBrightness));
            spriteCounter++;
            g2d.drawImage(bestImage, x , y , null);
        }

        g2d.dispose();
        return new Pair<BufferedImage,Integer>(newImage, spriteCounter);
    }

    public static void main(String[] args) throws IOException {
        String filePath = getFilePathFromUser();
        Mode selectedMode = getModeFromUser();
        if(selectedMode == null)
            return;

        File[] listOfFiles = new File("assets").listFiles();
        if (listOfFiles != null){
            Map<Integer, BufferedImage> imagesByColor = new HashMap<>();
            Octree colorsOctree = new Octree();
            //fill imagesByColor and colorsOctree
            processImages(listOfFiles, imagesByColor, colorsOctree);
            
            BufferedImage image = ImageIO.read(new File(filePath));
            Pair<BufferedImage,Integer> returnData  = recreateImage(colorsOctree, imagesByColor, image, selectedMode);        

            ImageIO.write(returnData.first(), "png", new File("output.png"));

            System.out.println(String.format("numero de sprites: %d", returnData.second()));
        }  
        
    }
    
    private enum Mode {
        X_4(4,1,"width * 4;  height * 4; num of sprites = num of pixels of the original image"),
        X_8(8,16,"width * 8;  height * 8; num of sprites = num of pixels of the original image * 16");

        private int scale;
        private int nspritesmodifier;
        private String description;

        Mode(int scale, int nspritesmodifier, String description){
            this.scale = scale;
            this.nspritesmodifier = nspritesmodifier;
            this.description = description;
        }

        public int getScale(){
            return scale;
        }
        public int getNSpritesModifier(){
            return nspritesmodifier;
        }
        
        public String getDescription(){
            return description;
        }
    }

}