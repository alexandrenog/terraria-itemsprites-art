
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;


public class Octree {
    private static final int MAX_POINTS_PER_NODE = 10;
  
    private OctreeNode root;
    public Octree() {
        root = new OctreeNode(new Point3D(1<<7,1<<7,1<<7),1<<7,null);
    };

    public void insert(Point3D p){
        root.insert(p);
    }
    public void insert(Color color){
        insert(new Point3D(color.getRed(),color.getGreen(),color.getBlue()));
    }

    private static class OctreeNode {
        private final Point3D center;
        private final int halfSize;
        private List<Point3D> points;
        private OctreeNode[] children;
        private OctreeNode parent;
    
        public OctreeNode(Point3D center, int halfSize,OctreeNode parent) {
            this.center = center;
            this.halfSize = halfSize;
            this.points = new ArrayList<>();
            this.parent = parent;
        }
  
        public void insert(Point3D p) {
            if (children != null) {
                // If the node has children, insert the point into the appropriate child
                int octant = getOctant(p);
                children[octant].insert(p);
            } else {
                // If the node doesn't have children, add the point to the list
                points.add(p);
        
                // If the number of points exceeds the maximum, split the node into 8 children
                if (points.size() > MAX_POINTS_PER_NODE) {
                    split();
                }
            }
        }
  
        private void split() {
            children = new OctreeNode[8];
            int childHalfSize = halfSize / 2;
            for (int i = 0; i < 8; i++) {
                int x = center.getX() + (i & 1) * childHalfSize;
                int y = center.getY() + (i & 2) * childHalfSize;
                int z = center.getZ() + (i & 4) * childHalfSize;
                Point3D childCenter = new Point3D(x, y, z);
                children[i] = new OctreeNode(childCenter, childHalfSize,this);
            }
    
            // Move the points from the current node into the appropriate child nodes
            for (Point3D p : points) {
                int octant = getOctant(p);
                children[octant].points.add(p);
            }
            points.clear();
        }

        private int getOctant(Point3D p) {
            int octant = 0;
            if (p.getX() > center.getX()) octant |= 1;
            if (p.getY() > center.getY()) octant |= 2;
            if (p.getZ() > center.getZ()) octant |= 4;
            return octant;
        }

    }

    public Point3D nearestNeighbor(Point3D target) {
        // Initialize the nearest point to be the root node's center
        Point3D nearest = root.center;
        double nearestDistance = target.distance(nearest);
    
        // Recursively search the octree for the nearest point
        nearest = nearestNeighbor(root, target, nearest, nearestDistance);
    
        return nearest;
    }
    
    private Point3D nearestNeighbor(OctreeNode node, Point3D target, Point3D nearest, double nearestDistance) {
        // Check if the current node is a leaf node
        if (node.children == null) {
            // If it's a leaf node, check all the points in the list and update the nearest point if necessary
            for (Point3D p : node.points) {
                double distance = target.distance(p);
                if (distance < nearestDistance) {
                    nearest = p;
                    nearestDistance = distance;
                }
            }
        } else {
            OctreeNode child = node.children[node.getOctant(target)];
            Point3D childNearest = nearestNeighbor(child, target, nearest, nearestDistance);
            double childDistance = target.distance(childNearest);
            if (childDistance < nearestDistance) {
                nearest = childNearest;
                nearestDistance = childDistance;
            }
        }
    
        return nearest;
    }
    
}
