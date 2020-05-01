package engine.objects;

import engine.GameGraphics;
import engine.util.*;
import mario.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class used to check for collisions between physics objects. All colliders are represented as a polygon made from n
 * points connected by n lines.
 *
 * @author Galen Savidge
 * @version 4/30/2020
 */
public class Collider {

    protected static ArrayList<Collider> colliders = new ArrayList<>(); // A list of all colliders that exist

    /**
     * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
     */
    public static class Collision {
        public Collider collider;
        public boolean collision_found;
        public ArrayList<PhysicsObject> collided_with = new ArrayList<>();
        public ArrayList<Vector2> intersections = new ArrayList<>();

        public Collision(Collider collider) {
            this.collider = collider;
            this.collision_found = false;
        }
    }

    /**
     * Adds {@code element} to {@code array} iff {@code array} does not contain an element mathematically equal to
     * {@code element}.
     */
    protected static void addNoDuplicates(ArrayList<Vector2> array, Vector2 element) {
        boolean contains_element = false;
        for(Vector2 v : array) {
            if(v.equals(element)) {
                contains_element = true;
                break;
            }
        }
        if(!contains_element) {
            array.add(element);
        }
    }

    /* Instance variables */

    protected PhysicsObject object; // The object this collider is attached to
    protected Vector2 position; // The coordinates of this collider relative to its attached object
    protected Vector2 center; // Center point of the collider; initially set to the mean of the vertices
    private final ArrayList<Vector2> local_vertices = new ArrayList<>(); // Vertices in local space
    private Collision last_collision; // Used for drawing


    /* Constructors */

    /**
     * @param object The {@code PhysicsObject} to which to attach.
     * @param local_vertices A list of vertices in clockwise order.
     */
    public Collider(PhysicsObject object, Vector2[] local_vertices) {
        colliders.add(this);
        this.object = object;
        this.position = Vector2.zero();
        this.local_vertices.addAll(Arrays.asList(local_vertices));
        this.center = Vector2.zero();
        for(Vector2 v : this.local_vertices) {
            this.center = this.center.add(v);
        }
        this.center = this.center.multiply(1.0/this.local_vertices.size());
    }

    /**
     * @param object The {@code PhysicsObject} to which to attach.
     * @param x_offset {@code X} offset of the top left corner.
     * @param y_offset {@code Y} offset of the top left corner.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     * @return A new Collider in the shape of a rectangle attached to {@code object}.
     */
    public static Collider newBox(PhysicsObject object, int x_offset, int y_offset, int width, int height) {
        Vector2[] corners = {new Vector2(x_offset,y_offset), new Vector2(x_offset+width-0.1,y_offset),
                new Vector2(x_offset+width-0.1,y_offset+height-0.1), new Vector2(x_offset,y_offset+height-0.1)};
        Collider collider = new Collider(object, corners);
        collider.setPosition(object.position);
        return collider;
    }

    /* Instance methods */

    /**
     * @return The position of the top left corner of the collider's bounding box.
     */
    public Vector2 getPosition() {
        return position.copy();
    }

    public void setPosition(Vector2 position) {
        this.position = position.copy();
    }

    /**
     * @return The center of this {@code Collider} in global space.
     */
    public Vector2 getCenter() {
        return center.add(position);
    }

    /**
     * @param center The new center for this {@code Collider} in local space.
     */
    public void setCenter(Vector2 center) {
        this.center = center.copy();
    }

    /**
     * Removes this collider from the global colliders list and removes its reference to the attached PhysicsObject.
     */
    public void delete() {
        colliders.remove(this);
        this.object = null;
    }

    /**
     * @return A list of the positions of the collider's vertices in global space.
     */
    public ArrayList<Vector2> getVertices() {
        ArrayList<Vector2> vertices = new ArrayList<>();
        for(Vector2 v : local_vertices) {
            vertices.add(v.add(position));
        }
        return vertices;
    }

    /**
     * @return A list of the line segments connecting the collider's vertices running clockwise.
     */
    public ArrayList<Line> getLines() {
        ArrayList<Vector2> vertices = this.getVertices();
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0;i < vertices.size();i++) {
            int j = i-1;
            if(j < 0) j += vertices.size();
            lines.add(new Line(vertices.get(j), vertices.get(i)));
        }
        return lines;
    }

    /**
     * Checks for collisions with all other Colliders in the game world.
     * @return A Collision object.
     */
    public Collision getCollisions() {
        return getCollisions(colliders);
    }

    /**
     * Checks for collisions with other Colliders.
     * @param colliders A list of colliders to check.
     * @return A Collision object.
     */
    public Collision getCollisions(ArrayList<Collider> colliders) {
        Collision collision = new Collision(this);

        // Get intersections
        for(Collider c : colliders) {
            if(c != this) {
                this.checkCollision(collision, c);
            }
        }

        return collision;
    }

    /**
     * Checks if this collider collides with other. This function should be overridden to handle collisions with
     * different collider types.
     */
    public void checkCollision(Collision collision, Collider other) {
        ArrayList<Line> this_lines = this.getLines();
        ArrayList<Line> other_lines = other.getLines();
        boolean collided_with_other = false;

        for(Line this_l : this_lines) {
            for(Line other_l : other_lines) {
                Vector2 p = this_l.intersection(other_l);
                if(p != null) {
                    collided_with_other = true;
                    addNoDuplicates(collision.intersections, p);
                }
            }
        }

        if(collided_with_other) {
            collision.collision_found = true;
            collision.collided_with.add(other.object);
        }

        last_collision = collision;
    }

    /**
     * Finds the normal vector of a surface in contact (< 1px) with the collider.
     *
     * @param direction Direction to check.
     * @return The normal vector.
     */
    public Vector2 getNormal(Vector2 direction, ArrayList<Collider> colliders) {
        // Set position
        Vector2 old_position = position;
        direction = direction.normalize();
        position = position.add(direction.multiply(1.5)); // Make a vector so that sqrt(2) < ||v|| < 2

        // Get mean point of intersections
        Collision collision = getCollisions(colliders);
        Vector2 mean = Vector2.zero();
        for(Vector2 p : collision.intersections) {
            mean = mean.add(p);
        }
        mean = mean.multiply(1.0/collision.intersections.size());

        // Get edges of colliders
        ArrayList<Line> edges = new ArrayList<>();
        for(Collider c : colliders) {
                edges.addAll(c.getLines());
        }

        // Raycast from mean to this Collider's center and find closest intersection to the center
        Vector2 ray_v = mean.subtract(this.getCenter());
        Line ray = new Line(this.getCenter(), this.getCenter().add(ray_v.multiply(10))); // This is lazy...
        Line shortest = null; // Line containing the intersection point closest to the center
        double shortest_dist = Double.MAX_VALUE;
        for(Line l : edges) {
            Vector2 i = ray.intersection(l);
            if(i != null) {
                double distance = (i.subtract(this.getCenter())).abs();
                if(distance < shortest_dist) {
                    shortest = l;
                    shortest_dist = distance;
                }
            }
        }


        // Reset position
        position = old_position;

        if(shortest != null) {
            return shortest.RHNormal();
        }
        else {
            return null;
        }
    }

    /**
     * Draws the edges of the collider as well as any recorded intersections from collision checks.
     */
    public void draw() {
        {
            GameGraphics.drawPoint((int)this.getCenter().x,(int)this.getCenter().y, false, Color.black);

            ArrayList<Line> lines = getLines();
            for(Line l : lines) {
                l.draw();
            }

            if(!(object instanceof Player)) {
                return;
            }

            if(last_collision != null) {
                for (Vector2 i : last_collision.intersections) {
                    GameGraphics.drawPoint((int) i.x, (int) i.y, false, Color.RED);
                }

                last_collision = null;
            }
        }
    }
}
