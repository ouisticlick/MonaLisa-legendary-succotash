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

    public static Color[][] source;
    public static int maxX, maxY;

    public final static int NB_GEN = 1000;
    public final static int NB_INDIVIDU = 100;
    public final static int NB_SELECT_BEST = 15;
    public final static int NB_SELECT_ALEA = 5;
    public final static int NB_POLY = 50;
    public final static int NB_SOMMETS = 3;
    public final static double DEFAULT_OPACITY = 0.15;
    public static double PROBA_SELECT_INDIVIDU = 1;
    public final static double PROBA_SELECT_POLYGONE = 0.01;
    public static double INTENSITE_MODIFICATION = 0.10;
    public static Random gen;

    public static void main(String[]args){
        gen = new Random(42);
        launch(args);
    }


    public static ConvexPolygon[] algoGen(Stage stage){

        Population pop_actuelle = new Population(NB_INDIVIDU, NB_POLY, NB_SOMMETS, DEFAULT_OPACITY);
        for(int i = 0; i < NB_GEN; i++){

            Population selectionnee = pop_actuelle.selection(NB_SELECT_BEST, NB_SELECT_ALEA);
            System.out.println("Génération : "+i+"; meilleur = "+pop_actuelle.getPopset()[0].cout);
            pop_actuelle = selectionnee.crossover(NB_INDIVIDU);
            pop_actuelle.mutation(PROBA_SELECT_INDIVIDU, PROBA_SELECT_POLYGONE, INTENSITE_MODIFICATION);
            if(i%10==0){
                draw(pop_actuelle.getPopset()[0].getCpset(), stage, ""+i);
            }
        }

        Population best = pop_actuelle.selection(1, 0);
        return best.getPopset()[0].getCpset();

    }

    public static void loadImage(){
        String targetImage = "monaLisa-100.jpg";
        try{
            BufferedImage bi = ImageIO.read(new File(targetImage));
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
    public void start(Stage stage) throws Exception {
        loadImage();
        ConvexPolygon[] cp = algoGen(stage);
        draw(cp, stage, "best");
    }

    public static void draw(ConvexPolygon[] cp, Stage myStage, String nom){
        Group image = new Group();
        for (ConvexPolygon p : cp)
            image.getChildren().add(p);
        WritableImage wimg = new WritableImage(maxX,maxY);
        image.snapshot(null,wimg);

        // Stockage de l'image dans un fichier .png
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(wimg, null);
        try {
            ImageIO.write(renderedImage, "png", new File("monalisa"+nom+".png"));
            System.out.println("wrote image");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // affichage de l'image dans l'interface graphique
        Scene scene = new Scene(image,maxX, maxY);
        myStage.setScene(scene);
        myStage.show();
    }
}
