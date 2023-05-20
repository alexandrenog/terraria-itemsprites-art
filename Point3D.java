import java.util.List;

public class Point3D {
    
    private int x,y,z;

    public Point3D(int x,int y,int z){
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getZ(){
        return z;
    }
    public double distance(Point3D other){
        int dx=x-other.x, dy=y-other.y, dz = z-other.z;
        return Math.sqrt((dx*dx)+(dy*dy)+(dz*dz));
    }
    public String toString(){
        return String.format("x=%d, y=%d, z=%d",x,y,z);
    }

    public static Point3D findClosestPoint(Point3D p, List<Point3D> points) {
        Point3D closestPoint = null;
        double closestDistance = Double.MAX_VALUE;
        for (Point3D point : points) {
            double distance = p.distance(point);
            if (distance < closestDistance) {
                closestPoint = point;
                closestDistance = distance;
            }
        }
        return closestPoint;
    }
}
