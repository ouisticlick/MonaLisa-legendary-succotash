import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KMeans {

    private int k;
    private Cluster[] clusters;
    private int max_iter;

    public KMeans(int k, int max_iter){
        this.k = k;
        this.clusters = new Cluster[k];
        this.max_iter = max_iter;
    }

    public void fit(Color[][] image){
        /***
         * Générer les prototypes
         */
        for(int i = 0; i < k; i++){
            this.clusters[i] = new Cluster(MonaLisa.gen.nextInt(image.length), MonaLisa.gen.nextInt(image[0].length));
        }
        for(int iter = 0; iter < this.max_iter; iter++){
            for(int i = 0; i < image.length; i++){
                for(int j = 0; j < image[0].length; j++){
                    Point pt = new Point(i,j,image[i][j]);
                    Cluster best = null;
                    double min_dist = 99999999;
                    for(Cluster cl : this.clusters){
                        double dist = cl.distance(pt);
                        if(dist < min_dist){
                            min_dist = dist;
                            best = cl;
                        }
                    }
                    best.affecter(pt);
                }
            }
        }
        for(Cluster cl : this.clusters){
            cl.recalculerCentre();
        }
    }

    public void display(Color[][] image){
        Color[] col_clus = new Color[k];
        for(int i = 0; i < k; i++){
            col_clus[i] = Color.rgb(
                    MonaLisa.gen.nextInt(255),
                    MonaLisa.gen.nextInt(255),
                    MonaLisa.gen.nextInt(255)
            );
        }
        Color[][] kmeans = new Color[image.length][image[0].length];
        for(int i = 0; i < kmeans.length; i++){
            for(int j = 0; j < kmeans[0].length; j++){
                int fav_id = -1;
                double min_dist = 99999999;
                for(int kk = 0; kk < k; kk++){
                    double dist = this.clusters[kk].distance(new Point(i,j,image[i][j]));
                    if(dist < min_dist){
                        min_dist = dist;
                        fav_id = kk;
                    }
                }
                //kmeans[i][j] = col_clus[fav_id];
                kmeans[i][j] = this.clusters[fav_id].color;
            }
        }
        WritableImage img = new WritableImage(image.length, image[0].length);
        PixelWriter pw = img.getPixelWriter();
        for(int i = 0; i < kmeans.length; i++){
            for(int j = 0; j < kmeans[0].length; j++){
                pw.setColor(i,j,kmeans[i][j]);
            }
        }
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(img, null);
        try {
            ImageIO.write(renderedImage, "png", new File("kmeans.png"));
            System.out.println("wrote image");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public double cout(Color[][] image){
        Color[][] kmeans = new Color[image.length][image[0].length];
        for(int i = 0; i < kmeans.length; i++){
            for(int j = 0; j < kmeans[0].length; j++){
                int fav_id = -1;
                double min_dist = 99999999;
                for(int kk = 0; kk < k; kk++){
                    double dist = this.clusters[kk].distance(new Point(i,j,image[i][j]));
                    if(dist < min_dist){
                        min_dist = dist;
                        fav_id = kk;
                    }
                }
                kmeans[i][j] = this.clusters[fav_id].color;
            }
        }
        double res = 0;
        for (int i = 0; i < MonaLisa.maxX; i++) {
            for (int j = 0; j < MonaLisa.maxY; j++) {
                Color c = kmeans[i][j];
                res += Math.pow(c.getBlue() - image[i][j].getBlue(), 2)
                        + Math.pow(c.getRed() - image[i][j].getRed(), 2)
                        + Math.pow(c.getGreen() - image[i][j].getGreen(), 2);
            }
        }
        res = Math.sqrt(res);
        return res;
    }

    public class Point{
        public int x;
        public int y;
        public Color color;

        public Point(int x, int y, Color c){
            this.x = x;
            this.y = y;
            this.color = c;
        }

        public double distance(Point p){
            return Math.sqrt(
                    Math.pow(this.x-p.x,2)
                            +Math.pow(this.y-p.y,2)
                            +Math.pow(this.color.getRed()-p.color.getRed(),2)
                            +Math.pow(this.color.getGreen()-p.color.getGreen(),2)
                            +Math.pow(this.color.getBlue()-p.color.getBlue(),2)
            );
        }
    }

    public class Cluster extends Point{

        public List<Point> affectes;

        public Cluster(int x, int y){
            super(x,y,Color.WHITE);
            affectes = new ArrayList<>();
        }

        public void affecter(Point p){
            affectes.add(p);
        }

        public void recalculerCentre(){
            double x = 0;
            double y = 0;
            double r = 0;
            double g = 0;
            double b = 0;
            for(Point p : affectes){
                x += p.x;
                y += p.y;
                r += p.color.getRed();
                g += p.color.getGreen();
                b += p.color.getBlue();
            }
            x /= affectes.size();
            y /= affectes.size();
            r /= affectes.size();
            g /= affectes.size();
            b /= affectes.size();
            this.x = (int)x;
            this.y = (int)y;
            this.color = Color.rgb(
                    (int)(r*255),
                    (int)(g*255),
                    (int)(b*255)
            );
            affectes = new ArrayList<>();
        }

    }

}
