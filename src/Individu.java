import javafx.scene.Group;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Individu {

    // Un individu est un ensemble de polygones convexes
    ConvexPolygon[] cpset;
    private int sommets;
    private double opacity;
    public double cout = -1;


    public Individu(int nbpoly, int sommets, double opacity) {
        this.sommets = sommets;
        this.opacity = opacity;
        cpset = new ConvexPolygon[nbpoly];
        for (int i = 0; i < nbpoly; i++) {
            cpset[i] = new ConvexPolygon(sommets, opacity);
        }
    }

    public Individu(ConvexPolygon[] pols){
        this.cpset = Arrays.copyOf(pols, pols.length);
        //todo peut etre source de problemes ?
        this.sommets = cpset[0].getPoints().size()/2;
        this.opacity = cpset[0].getOpacity();
    }

    // constructeur par copie
    public Individu(Individu in) {
        cpset = new ConvexPolygon[in.cpset.length];
        this.sommets = in.sommets;
        this.opacity = in.opacity;
        for (int i = 0; i < in.cpset.length; i++) {
            cpset[i] = new ConvexPolygon(in.cpset[i]);
        }
    }

    //todo arranger, bricolage
    public Individu(int nbpoly, int sommets, double opacity, Object diff) {
        this.sommets = sommets;
        this.opacity = opacity;
        cpset = new ConvexPolygon[nbpoly];
    }

    // calcule la fitness d'un individu
    public double calcFitness() {
        Group image = new Group();
        for (ConvexPolygon p : cpset)
            image.getChildren().add(p);


        WritableImage wimg = new WritableImage(MonaLisa.maxX, MonaLisa.maxY);
        image.snapshot(null, wimg);
        PixelReader pr = wimg.getPixelReader();

        double res = 0;
        for (int i = 0; i < MonaLisa.maxX; i++) {
            for (int j = 0; j < MonaLisa.maxY; j++) {
                Color c = pr.getColor(i, j);
                res += Math.pow(c.getBlue() - MonaLisa.source[i][j].getBlue(), 2)
                        + Math.pow(c.getRed() - MonaLisa.source[i][j].getRed(), 2)
                        + Math.pow(c.getGreen() - MonaLisa.source[i][j].getGreen(), 2);
            }
        }
        res = Math.sqrt(res);
        cout = res;
        return res;
    }

    //fonction de mutation avec proba1 et intensité changmt
    public void mutation(double proba1, double chgmt) {
        assert cpset.length >= 1;
        for (int i = 0; i < cpset.length; i++) {
            if (MonaLisa.gen.nextDouble() < proba1) {
                //cpset[i] = new ConvexPolygon(this.sommets, this.opacity);
                double red = ((Color) cpset[i].getFill()).getRed();
                double green = ((Color) cpset[i].getFill()).getGreen();
                double blue = ((Color) cpset[i].getFill()).getBlue();
                double opacity = ((Color) cpset[i].getFill()).getOpacity();
                // les couleurs varient selon l'intensité de changement donnée
                red += MonaLisa.gen.nextDouble() * chgmt * 2 - chgmt;
                green += MonaLisa.gen.nextDouble() * chgmt * 2 - chgmt;
                blue += MonaLisa.gen.nextDouble() * chgmt * 2 - chgmt;
                opacity += MonaLisa.gen.nextDouble() * chgmt * 2 - chgmt;
                //appel à la fonction normalize
                red = normalize(red);
                green = normalize(green);
                blue = normalize(blue);
                opacity = normalize(opacity);
                cpset[i].setFill(Color.rgb((int) (255 * red),
                        (int) (255 * green),
                        (int) (255 * blue),
                        opacity));
                //maintenant on fait muter les polygones de l'individu
                for (int j = 0; j < cpset[i].getPoints().size(); j++) {
                    //if(MonaLisa.gen.nextDouble()<proba1){
                    //CAS X PUIS CAS Y
                    if (i % 2 == 0) {
                        double coord = (cpset[i].getPoints().get(j) + (MonaLisa.gen.nextDouble() * MonaLisa.maxX * chgmt * 2) - MonaLisa.maxX * chgmt);
                        coord = coord > MonaLisa.maxX ? MonaLisa.maxX : coord;
                        coord = coord < 0 ? 0 : coord;
                        cpset[i].getPoints().set(j, coord);
                    } else {
                        double coord = (cpset[i].getPoints().get(j) + (MonaLisa.gen.nextDouble() * MonaLisa.maxY * chgmt * 2) - MonaLisa.maxY * chgmt);
                        coord = coord > MonaLisa.maxY ? MonaLisa.maxY : coord;
                        coord = coord < 0 ? 0 : coord;
                        cpset[i].getPoints().set(j, coord);
                    }
                }
            }
        }
    }

    // ramène un double dans l'intervalle [0,1]
    private double normalize(double nb) {
        return nb < 0 ? 0 : nb > 1 ? 1 : nb;
    }

    // fonction crossover
    public Individu crossover(Individu x) {
        Individu fils = new Individu(this.cpset.length, this.sommets, this.opacity, null);
        List<ConvexPolygon> liste = new ArrayList<>();
        for (int i = 0; i < this.cpset.length; i++) {
            if (MonaLisa.gen.nextDouble()<0.5) {
                liste.add(new ConvexPolygon(this.cpset[i]));
            } else {
                liste.add(new ConvexPolygon(this.cpset[i]));
            }
        };
        fils.cpset = liste.toArray(fils.cpset);
        assert fils.cpset.length == this.cpset.length;
        return fils;
    }


    public Individu crossover2(Individu x) {
        Individu fils = new Individu(this.cpset.length, this.sommets, this.opacity, null);
        List<Integer> index1 = new ArrayList<>();
        List<Integer> index2 = new ArrayList<>();
        for (int i = 0; i < this.cpset.length; i++) {
            index1.add(i);
            index2.add(i);
        }
        Collections.shuffle(index1, MonaLisa.gen);
        Collections.shuffle(index2, MonaLisa.gen);
        List<ConvexPolygon> liste = new ArrayList<>();
        for (int i = 0; i < this.cpset.length; i++) {
            if (i < (this.cpset.length / 2)) {
                liste.add(new ConvexPolygon(this.cpset[index1.get(i)]));
            } else {
                liste.add(new ConvexPolygon(this.cpset[index2.get(i)]));
            }
        }
        Collections.shuffle(liste, MonaLisa.gen);
        fils.cpset = liste.toArray(fils.cpset);
        assert fils.cpset.length == this.cpset.length;
        return fils;
    }

    public Individu crossover3(Individu x){
        Individu fils = new Individu(this.cpset.length, this.sommets, this.opacity, null);
        List<Integer> index1 = new ArrayList<>();
        List<Integer> index2 = new ArrayList<>();
        for (int i = 0; i < this.cpset.length; i++) {
            index1.add(i);
            index2.add(i);
        }
        Collections.shuffle(index1, MonaLisa.gen);
        Collections.shuffle(index2, MonaLisa.gen);
        List<ConvexPolygon> liste = new ArrayList<>();
        for (int i = 0; i < this.cpset.length; i++) {
            liste.add(this.cpset[index1.get(i)].crossover(x.cpset[index2.get(i)]));
        }
        Collections.shuffle(liste, MonaLisa.gen);
        fils.cpset = liste.toArray(fils.cpset);
        assert fils.cpset.length == this.cpset.length;
        return fils;
    }

    public ConvexPolygon[] getCpset() {
        return cpset;
    }

}
