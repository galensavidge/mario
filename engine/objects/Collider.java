package engine.objects;

import engine.GameGraphics;
import engine.util.*;
import mario.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The base class for collider objects, which are used to create and check hitboxes for game objects.
 *
 * @author Galen Savidge
 * @version 4/26/2020
 */
public class Collider {
    /* Static class methods */

    protected static ArrayList<Collider> colliders = new ArrayList<>();

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
    private final ArrayList<Vector2> local_vertices = new ArrayList<>();
    private Collision last_collision; // Used for drawing

    /* Constructors */

    /**
     *
     * @param object
     * @param local_vertices
     */
    public Collider(PhysicsObject object, Vector2[] local_vertices) {
        colliders.add(this);
        this.object = object;
        this.position = Vector2.zero();
        this.local_vertices.addAll(Arrays.asList(local_vertices));
    }

    /**
     *
     * @param object
     * @param x_offset
     * @param y_offset
     * @param width
     * @param height
     * @return
     */
    public static Collider newBox(PhysicsObject object, int x_offset, int y_offset, int width, int height) {
        Vector2[] corners = {new Vector2(x_offset,y_offset), new Vector2(x_offset+width-1,y_offset),
                new Vector2(x_offset+width-1,y_offset+height-1), new Vector2(x_offset,y_offset+height-1)};
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
     * Removes this collider from the global colliders list and removes its reference to the attached PhysicsObject.
     */
    public void delete() {
        colliders.remove(this);
        this.object = null;
    }

    /**
     *
     * @return
     */
    public ArrayList<Vector2> getVertices() {
        ArrayList<Vector2> vertices = new ArrayList<>();
        for(Vector2 v : local_vertices) {
            vertices.add(v.add(position));
        }
        return vertices;
    }

    /**
     *
     * @return
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
     *
     * @param direction Direction to check.
     * @return The normal vector.
     */
    public Vector2 getNormal(Vector2 direction, ArrayList<Collider> colliders) {
        // Set position
        Vector2 old_position = position;
        direction = direction.normalize();
        position = position.add(direction.multiply(1.5)); // Make a vector so sqrt(2) < ||v|| < 2

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

        // Raycast from mean in both directions
        Line ray = new Line(mean.add(direction.multiply(16)), mean.add(direction.multiply(-16))); // Needs updating
        Line shortest = null;
        double shortest_dist = Double.MAX_VALUE;
        for(Line l : edges) {
            Vector2 i = ray.intersection(l);
            if(i != null) {
                double distance = (i.subtract(mean)).abs(); // Needs updating
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
     *
     */
    public void draw() {
        {
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
