import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMeans {

    private int k;
    private Cluster[] clusters;
    private int max_iter;
    private int coeff_color;

    public KMeans(int k, int max_iter, int coeff_color) {
        this.k = k;
        this.clusters = new Cluster[k];
        this.max_iter = max_iter;
        this.coeff_color = coeff_color;
    }

    /**
     * Trouve les meilleurs k-clusters pour une image
     *
     * @param image
     */
    public void fit(Color[][] image) {
        /***
         * Générer les prototypes
         */
        for (int i = 0; i < k; i++) {
            this.clusters[i] = new Cluster(MonaLisa.gen.nextInt(image.length), MonaLisa.gen.nextInt(image[0].length));
        }
        for (int iter = 0; iter < this.max_iter; iter++) {
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    Point pt = new Point(i, j, image[i][j]);
                    Cluster best = null;
                    double min_dist = 99999999;
                    for (Cluster cl : this.clusters) {
                        double dist = cl.distance(pt);
                        if (dist < min_dist) {
                            min_dist = dist;
                            best = cl;
                        }
                    }
                    best.affecter(pt);
                }
            }
        }
        for (Cluster cl : this.clusters) {
            cl.recalculerCentre();
        }
    }

    public Color[][] predict(Color[][] image) {
        Color[][] colors = new Color[image.length][image[0].length];
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                int fav_id = -1;
                double min_dist = Double.POSITIVE_INFINITY;
                for (int kk = 0; kk < k; kk++) {
                    double dist = this.clusters[kk].distance(new Point(i, j, image[i][j]));
                    if (dist < min_dist) {
                        min_dist = dist;
                        fav_id = kk;
                    }
                }
                colors[i][j] = this.clusters[fav_id].color;
            }
        }
        return colors;
    }

    /**
     * Pour une image, l'affiche
     *
     * @param kmeans
     */
    public void render(Color[][] kmeans, String filename) {
        WritableImage img = new WritableImage(kmeans.length, kmeans[0].length);
        PixelWriter pw = img.getPixelWriter();
        for (int i = 0; i < kmeans.length; i++) {
            for (int j = 0; j < kmeans[0].length; j++) {
                pw.setColor(i, j, kmeans[i][j]);
            }
        }
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(img, null);
        try {
            ImageIO.write(renderedImage, "png", new File(filename));
            System.out.println("wrote image");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Pour une image des affectations des pixels et l'image, calcule le cout
     *
     * @param image
     * @param kmeans
     * @return
     */
    public double cout(Color[][] image, Color[][] kmeans) {
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

    // source : https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
    // https://github.com/mission-peace/interview/blob/master/src/com/interview/geometry/JarvisMarchConvexHull.java
    /**
     * Jarvis march
     *
     * @param points
     * @return
     */

    public List<Point> jarvis_march(Point[] points) {
        List<Point> list=new ArrayList<>();
        if(points==null || points.length<1) return list;

        int leftmost=0;
        for(int i=1;i<points.length;i++){
            if(points[leftmost].x>points[i].x){
                leftmost=i;
            }
        }

        boolean[] visited=new boolean[points.length];

        int current=leftmost;
        int next;

        do{
            // visited[current]=true;
            list.add(points[current]);
            visited[current]=true;
            next=(current+1)%points.length;

            for(int i=0;i<points.length;i++){
                int val=crossproduct(points[current],points[next],points[i]);
                if(val<0){
                    next=i;
                }else if(val==0&&distance(points[current],points[i])>distance(points[current],points[next])){
                    next=i;
                }

            }

            for(int i=0;i<points.length;i++){
                if(i!=next&&i!=current&&crossproduct(points[current],points[next],points[i])==0&& !visited[i]){
                    visited[i]=true;
                    list.add(points[i]);
                }
            }

            current=next;
        }while(next!=leftmost);


        return list;
    }


    public int distance(Point a,Point b){
        return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y);
    }

    //cross product of pq \times pi
    public int crossproduct(Point p, Point q,Point i){
        int[] vectorpq=new int[]{q.x-p.x,q.y-p.y};
        int[] vectorpi=new int[]{i.x-p.x,i.y-p.y};

        return vectorpq[0]*vectorpi[1]-vectorpq[1]*vectorpi[0];
    }

    /**
     * Pour une image, renvoie la liste des polygones qui l'approxime
     *
     * @param image
     * @return
     */
    public ConvexPolygon[] clustersToPolygons(Color[][] image) {
        ConvexPolygon[] pols = new ConvexPolygon[k];
        List<List<Point>> pointsParCluster = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            pointsParCluster.add(new ArrayList<>());
        }
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                int fav_id = -1;
                double min_dist = Double.POSITIVE_INFINITY;
                for (int kk = 0; kk < k; kk++) {
                    double dist = this.clusters[kk].distance(new Point(i, j, image[i][j]));
                    if (dist < min_dist) {
                        min_dist = dist;
                        fav_id = kk;
                    }
                }
                pointsParCluster.get(fav_id).add(new Point(i, j, image[i][j]));
            }
        }
        for (int kk = 0; kk < k; kk++) {
            List<Point> coins = this.jarvis_march(pointsParCluster.get(kk).toArray(new Point[0]));
            pols[kk] = new ConvexPolygon(coins, 0.9, this.clusters[kk].color);
        }
        return pols;
    }

    /**
     * CoutPolygones
     *
     * @param pols
     * @return
     */
    public double coutPolygones(ConvexPolygon[] pols, Color[][] source) {
        Group image = new Group();
        for (ConvexPolygon p : pols)
            image.getChildren().add(p);


        WritableImage wimg = new WritableImage(source.length, source[0].length);
        image.snapshot(null, wimg);
        PixelReader pr = wimg.getPixelReader();

        double res = 0;
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[0].length; j++) {
                Color c = pr.getColor(i, j);
                res += Math.pow(c.getBlue() - source[i][j].getBlue(), 2)
                        + Math.pow(c.getRed() - source[i][j].getRed(), 2)
                        + Math.pow(c.getGreen() - source[i][j].getGreen(), 2);
            }
        }
        res = Math.sqrt(res);
        return res;
    }


    public class Point {
        public int x;
        public int y;
        public Color color;

        public Point(int x, int y, Color c) {
            this.x = x;
            this.y = y;
            this.color = c;
        }

        public double distance(Point p) {

            return Math.sqrt(
                    Math.pow(this.x - p.x, 2)
                            + Math.pow(this.y - p.y, 2)
                            + Math.pow((this.color.getRed() - p.color.getRed()) * coeff_color, 2)
                            + Math.pow((this.color.getGreen() - p.color.getGreen()) * coeff_color, 2)
                            + Math.pow((this.color.getBlue() - p.color.getBlue()) * coeff_color, 2)
            );
        }

        /**
         * On compare uniquement les coordonnées pas les couleurs
         *
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point point = (Point) o;
            return x == point.x &&
                    y == point.y;
        }
    }

    public class Cluster extends Point {

        public List<Point> affectes;

        public Cluster(int x, int y) {
            super(x, y, Color.WHITE);
            affectes = new ArrayList<>();
        }

        public void affecter(Point p) {
            affectes.add(p);
        }

        public void recalculerCentre() {
            double x = 0;
            double y = 0;
            double r = 0;
            double g = 0;
            double b = 0;
            for (Point p : affectes) {
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
            this.x = (int) x;
            this.y = (int) y;
            this.color = Color.rgb(
                    (int) (r * 255),
                    (int) (g * 255),
                    (int) (b * 255)
            );
            affectes = new ArrayList<>();
        }

    }

}
