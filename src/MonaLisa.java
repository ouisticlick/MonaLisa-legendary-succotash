import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MonaLisa extends Application {

    static Color[][] source;
    static int maxX, maxY;

    private final static int NB_GEN = 5000;
    private final static int NB_INDIVIDU = 100;
    private final static int NB_SELECT_BEST = 15;
    private final static int NB_SELECT_ALEA = 5;
    private final static int NB_POLY = 50;
    private final static double PROBA_SELECT_INDIVIDU = 1;
    private final static double PROBA_SELECT_POLYGONE = 0.01;
    private final static double INTENSITE_MODIFICATION = 0.02;
    private final static int NB_ITER_KMEANS = 300;
    static Random gen;
    private final static String IMAGE_FILE =  "data/input/monaLisa-200.jpg";
    private final static int COEFF_COLOR_DIST = 150;


    public static void main(String[]args){
        gen = new Random(42);
        launch(args);
    }


    public static ConvexPolygon[] algoGen(Stage stage, ConvexPolygon[] pols){

        Population pop_actuelle = new Population(pols);
        pop_actuelle = pop_actuelle.crossover(NB_INDIVIDU);
        for(int i = 0; i < NB_GEN; i++){

            Population selectionnee = pop_actuelle.selection(NB_SELECT_BEST, NB_SELECT_ALEA);
            System.out.println("Génération : "+i+"; meilleur = "+pop_actuelle.getPopset()[0].cout);
            pop_actuelle = selectionnee.crossover(NB_INDIVIDU);
            pop_actuelle.mutation(PROBA_SELECT_INDIVIDU, PROBA_SELECT_POLYGONE, INTENSITE_MODIFICATION);
            if(i%50==0){
                draw(pop_actuelle.getPopset()[0].getCpset(), stage, "data/evolution/"+i+".png", false);
            }
        }

        Population best = pop_actuelle.selection(1, 0);
        return best.getPopset()[0].getCpset();

    }

    public static void loadImage(){
        try{
            BufferedImage bi = ImageIO.read(new File(IMAGE_FILE));
            maxX = bi.getWidth();
            maxY = bi.getHeight();
            ConvexPolygon.max_X= maxX;
            ConvexPolygon.max_Y= maxY;
            source = new Color[maxX][maxY];
            for (int i=0;i<maxX;i++){
                for (int j=0;j<maxY;j++){
                    int argb = bi.getRGB(i, j);
                    int b = (argb)&0xFF;
                    int g = (argb>>8)&0xFF;
                    int r = (argb>>16)&0xFF;
                    int a = (argb>>24)&0xFF;
                    source[i][j] = Color.rgb(r,g,b);
                }
            }
        }
        catch(IOException e){
            System.err.println(e);
            System.exit(9);
        }
    }

    @Override
    public void start(Stage stage) {
        loadImage();
        KMeans k = new KMeans(NB_POLY, NB_ITER_KMEANS, COEFF_COLOR_DIST);
        k.fit(MonaLisa.source);
        System.out.println("Clusters par KMeans");
        Color[][] predict = k.predict(MonaLisa.source);
        k.render(predict,"data/output/kmeans_clusters.png");
        System.out.println("Cout : "+k.cout(MonaLisa.source, predict));



        /*
         * Polygones par KMeans
         */
        System.out.println("Polygones induits par jarvis march");
        ConvexPolygon[] pols = k.clustersToPolygons(MonaLisa.source);
        System.out.println("Cout : "+k.coutPolygones(pols, MonaLisa.source));
        draw(pols, stage, "data/output/convex_hull.png", false);


        /*
         * Algogen
         */
        ConvexPolygon[] cp = algoGen(stage, pols);
        draw(cp, stage, "data/output/algo_gen.png", true);
    }

    public static void draw(ConvexPolygon[] cp, Stage myStage, String filename, boolean display){
        Group image = new Group();
        for (ConvexPolygon p : cp)
            image.getChildren().add(p);
        WritableImage wimg = new WritableImage(maxX,maxY);
        image.snapshot(null,wimg);

        // Stockage de l'image dans un fichier .png
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(wimg, null);
        try {
            ImageIO.write(renderedImage, "png", new File(filename));
            System.out.println("wrote image");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // affichage de l'image dans l'interface graphique
        if(display) {
            Scene scene = new Scene(image, maxX, maxY);
            myStage.setScene(scene);
            myStage.show();
        }
    }
}
