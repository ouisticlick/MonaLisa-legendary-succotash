import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;



public class ConvexPolygon extends Polygon implements Serializable {
		
		static final int maxNumPoints=3;
		static int max_X,max_Y;
		NumberFormat nf = new DecimalFormat("##.00");
		
		
		// randomly generates a polygon
		public ConvexPolygon(int numPoints, double opacity){
			super();
			genRandomConvexPolygone(numPoints);
			int r = MonaLisa.gen.nextInt(256);
			int g = MonaLisa.gen.nextInt(256);
			int b = MonaLisa.gen.nextInt(256);
			this.setFill(Color.rgb(r, g, b));
			this.setOpacity(opacity);
		}


		// randomly generates a polygon
		public ConvexPolygon(int numPoints, double opacity, Color c){
			super();
			genRandomConvexPolygone(numPoints);
			this.setFill(c);
			this.setOpacity(opacity);
		}
		
		public ConvexPolygon(){
			super();
		}

		//constructeur par copie
		public ConvexPolygon(ConvexPolygon cp){
			this.nf = cp.nf;
			for(int i = 0; i < cp.getPoints().size()-1; i+=2){
				this.addPoint(cp.getPoints().get(i), cp.getPoints().get(i+1));
			}
			Color c = (Color) cp.getFill();
			this.setFill(Color.rgb((int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)));
			this.setOpacity(cp.getOpacity());
		}
		
		public String toString(){
			String res = super.toString();
			res += " " + this.getFill() + " opacity " + this.getOpacity();
			return res;
		}
			
		public void addPoint(double x, double y){
			getPoints().add(x);
			getPoints().add(y);
		}
		
		// http://cglab.ca/~sander/misc/ConvexGeneration/convex.html
		public void genRandomConvexPolygone(int n){
			List<Point> points = new LinkedList<Point>();
			List<Integer> abs = new ArrayList<>();
			List<Integer> ord = new ArrayList<>();
			
			for (int i=0;i<n;i++){
				abs.add(MonaLisa.gen.nextInt(max_X));
				ord.add(MonaLisa.gen.nextInt(max_Y));
			}
			Collections.sort(abs);
			Collections.sort(ord);
			//System.out.println(abs + "\n" + ord);
			int minX = abs.get(0);
			int maxX = abs.get(n-1);
			int minY = ord.get(0);
			int maxY = ord.get(n-1);
			
			List<Integer> xVec = new ArrayList<>();
			List<Integer> yVec = new ArrayList<>();
			
			int top= minX, bot = minX;
			for (int i=1;i<n-1;i++){
				int x = abs.get(i);
				
				if (MonaLisa.gen.nextBoolean()){
					xVec.add(x-top);
					top = x;
				} else{
					xVec.add(bot-x);
					bot = x;
				}
			}
			xVec.add(maxX-top);
			xVec.add(bot-maxX);
			
			int left= minY, right = minY;
			for (int i=1;i<n-1;i++){
				int y = ord.get(i);
				
				if (MonaLisa.gen.nextBoolean()){
					yVec.add(y-left);
					left = y;
				} else{
					yVec.add(right-y);
					right = y;
				}
			}
			yVec.add(maxY-left);
			yVec.add(right-maxY);
			
			Collections.shuffle(yVec, MonaLisa.gen);
			
			List<Point> lpAux = new ArrayList<>();
			for (int i=0;i<n;i++)
				lpAux.add(new Point(xVec.get(i), yVec.get(i)));
		
			
			// sort in order by angle
			Collections.sort(lpAux, (x,y) ->  Math.atan2(x.getY(), x.getX())  < Math.atan2(y.getY(), y.getX()) ? -1 :
				Math.atan2(x.getY(), x.getX())  == Math.atan2(y.getY(), y.getX()) ? 0 : 1);
				
			int x=0,y=0;
			int minPolX=0, minPolY=0;
			
			for (int i=0;i<n;i++){
				points.add(new Point(x,y));
				x += lpAux.get(i).getX();
				y += lpAux.get(i).getY(); 
				
				if (x < minPolX)
					minPolX=x;
				if (y<minPolY)
					minPolY=y;
			}
				
			int xshift = MonaLisa.gen.nextInt(max_X - (maxX-minX)) ;
			int yshift = MonaLisa.gen.nextInt(max_Y - (maxY-minY)) ;
			xshift -= minPolX;
			yshift -= minPolY;
			for (int i=0;i<n;i++){
				Point p = points.get(i);
				p.translate(xshift,yshift);
			}
			for (Point p : points)
				addPoint(p.getX(), p.getY());
			
		}
		
		public ConvexPolygon crossover(ConvexPolygon p){
			ConvexPolygon fils = new ConvexPolygon();
			ArrayList<Double> points = new ArrayList<>();
			/// On suppose des polygones de même nombre d'angles
			for(int i = 0; i < this.getPoints().size(); i++){
				points.add(MonaLisa.gen.nextDouble()<0.5 ? this.getPoints().get(i) : p.getPoints().get(i));
			}
			for(int i = 0; i < this.getPoints().size()-1; i+=2){
				fils.addPoint(points.get(i),points.get(i+1));
			}
			Color pere1 = (Color)this.getFill();
			int r1 = (int)(pere1.getRed()*255);
			int g1 = (int)(pere1.getGreen()*255);
			int b1 = (int)(pere1.getBlue()*255);
			double o1 = pere1.getOpacity();
			Color pere2 = (Color)p.getFill();
			int r2 = (int)(pere2.getRed()*255);
			int g2 = (int)(pere2.getGreen()*255);
			int b2 = (int)(pere2.getBlue()*255);
			double o2 = pere2.getOpacity();
			double o = MonaLisa.gen.nextDouble()<0.5 ? o1 : o2;
			o = o> 0.5 ? 0.5 : o;
			fils.setFill(Color.rgb(
					MonaLisa.gen.nextDouble()<0.5 ? r1 : r2,
					MonaLisa.gen.nextDouble()<0.5 ? g1 : g2,
					MonaLisa.gen.nextDouble()<0.5 ? b1 : b2
			));
			fils.setOpacity(o);
			return fils;
		}
	
		
		
		public class Point {

			int x,y;

			// generate a random point
			public Point(){
				x= MonaLisa.gen.nextInt(max_X);
				y= MonaLisa.gen.nextInt(max_Y);
			}
			
			public Point(int x, int y){
				this.x=x;
				this.y=y;
			}
			
			public int getX(){return x;}
			public int getY(){return y;}
			public void translate(int vx,int vy){
				x += vx;
				y += vy;
			}
			
			public boolean equals(Object o){
				if (o==null)
					return false;
				else if (o == this)
					return true;
				else if (o instanceof Point)
					return ((Point) o).x== this.x && ((Point) o).y== this.y;
				else
					return false;
			}
			
			public String toString(){
				NumberFormat nf = new DecimalFormat("#.00");
				return "(" + x + "," + y+")"; // + nf.format(Math.atan2(y, x))+")";
			}
			
		}
		
		
	
}
