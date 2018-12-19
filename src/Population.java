import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Population {

    Individu[] popset;

    public Population(int nbindividu, int nbpoly, int sommets, double opacity){
        popset = new Individu[nbindividu];
        for(int i = 0; i < nbindividu; i++){
            popset[i] = new Individu(nbpoly, sommets, opacity);
        }
    }

    public Population(ConvexPolygon[] pols){
        popset = new Individu[1];
        popset[0] = new Individu(pols);
    }

    public Population(int nbindividu){
        popset = new Individu[nbindividu];
    }

    public Population(Population p){
        popset = new Individu[p.popset.length];
        for(int i = 0; i < p.popset.length; i++){
            popset[i] = new Individu(p.popset[i]);
        }
    }

    public List<Individu> calcFitness(){
        for(int i = 0; i < popset.length; i++){
             popset[i].calcFitness();
        }
        List<Individu> atrier = new ArrayList<>();
        for (Individu i : popset){
            atrier.add(i);
        }
        Collections.sort(atrier, (o1, o2) -> o1.cout-o2.cout < 0 ? -1 : (o1.cout-o2.cout == 0 ? 0 : 1));
        return atrier;
    }

    public Population selection(int nbselect, int alea){
        List<Individu> tries = calcFitness();
        int taille = tries.size();
        Population newp = new Population(nbselect+alea);
        for(int i = 0; i < nbselect; i++){
            newp.popset[i] = tries.get(0);
            tries.remove(0);
        }
        Collections.shuffle(tries, MonaLisa.gen);
        for(int i = nbselect; i < nbselect+alea; i++){
            newp.popset[i] = tries.get(0);
            tries.remove(0);
        }
        assert tries.size() == taille-(nbselect+alea);
        return newp;
    }

    public void mutation(double proba1, double proba2, double proba3){
        for(Individu i : popset){
            if(MonaLisa.gen.nextDouble()<proba1){
                i.mutation(proba2, proba3);
            }
        }
    }

    public Population crossover(int nbindividu){
        Population newp = new Population(nbindividu);
        for(int i = 0; i < nbindividu; i++){
            int k = MonaLisa.gen.nextInt(this.popset.length);
            int j;
            do {
                j = MonaLisa.gen.nextInt(this.popset.length);
            } while(j==k && this.popset.length>1);
            newp.popset[i] = this.popset[k].crossover(this.popset[j]);
        }
        return newp;
    }

    public Individu[] getPopset(){
        return  popset;
    }

}
