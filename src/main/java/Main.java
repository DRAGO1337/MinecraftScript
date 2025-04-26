
import java.util.*;

public class Main {
    public static void main(String[] args) {
        PathFinder pathFinder = new PathFinder();
        Position start = new Position(0, 0, 0);
        Position goal = new Position(10, 0, 10);
        
        List<Position> path = pathFinder.findPath(start, goal);
        if (path != null) {
            System.out.println("Path found!");
            for (Position pos : path) {
                System.out.println(pos);
            }
        } else {
            System.out.println("No path found!");
        }
    }
}

class Position {
    int x, y, z;
    
    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position pos = (Position) o;
        return x == pos.x && y == pos.y && z == pos.z;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
    
    @Override
    public String toString() {
        return String.format("Position(%d, %d, %d)", x, y, z);
    }
    
    public double distanceTo(Position other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + 
                        Math.pow(y - other.y, 2) + 
                        Math.pow(z - other.z, 2));
    }
}

class PathFinder {
    private static final int[][] DIRECTIONS = {
        {1, 0, 0}, {-1, 0, 0},  // x axis
        {0, 1, 0}, {0, -1, 0},  // y axis
        {0, 0, 1}, {0, 0, -1}   // z axis
    };
    
    public List<Position> findPath(Position start, Position goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Position> closedSet = new HashSet<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Double> gScore = new HashMap<>();
        
        openSet.add(new Node(start, 0, start.distanceTo(goal)));
        gScore.put(start, 0.0);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.pos.equals(goal)) {
                return reconstructPath(cameFrom, current.pos);
            }
            
            closedSet.add(current.pos);
            
            for (int[] dir : DIRECTIONS) {
                Position neighbor = new Position(
                    current.pos.x + dir[0],
                    current.pos.y + dir[1],
                    current.pos.z + dir[2]
                );
                
                if (closedSet.contains(neighbor)) continue;
                
                double tentativeGScore = gScore.get(current.pos) + 1;
                
                if (!gScore.containsKey(neighbor) || 
                    tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.pos);
                    gScore.put(neighbor, tentativeGScore);
                    double fScore = tentativeGScore + neighbor.distanceTo(goal);
                    openSet.add(new Node(neighbor, tentativeGScore, fScore));
                }
            }
        }
        
        return null;
    }
    
    private List<Position> reconstructPath(Map<Position, Position> cameFrom, 
                                        Position current) {
        List<Position> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        
        return path;
    }
}

class Node implements Comparable<Node> {
    Position pos;
    double gScore;
    double fScore;
    
    public Node(Position pos, double gScore, double fScore) {
        this.pos = pos;
        this.gScore = gScore;
        this.fScore = fScore;
    }
    
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fScore, other.fScore);
    }
}
