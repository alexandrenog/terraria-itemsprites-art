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

}
