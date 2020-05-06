package engine.objects;

import engine.GameGraphics;
import engine.World;
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
public class Collider extends GameObject {

    /* Static Collider class */

    private static int zone_size;
    protected static ArrayList<Collider>[][] colliders; // A list of all colliders that exist

    public static final double edge_separation = 10*Misc.delta;
    public static final double reject_separation = 2*Misc.delta;


    public static void initColliders(int zone_size) throws ExceptionInInitializerError {
        if(World.getGridScale() == 0) {
            throw new ExceptionInInitializerError("World not initialized!");
        }

        Collider.zone_size = zone_size;

        int grid_width = World.getWidth()/zone_size + 1;
        int grid_height = World.getHeight()/zone_size + 1;
        colliders = new ArrayList[grid_width][grid_height];
        for(int i = 0;i < grid_width;i++) {
            for(int j = 0;j < grid_height;j++) {
                colliders[i][j] = new ArrayList<>();
            }
        }
    }

    private static void addToCollidersArray(Collider c) {
        int zone_x = Math.min(Math.max(0, (int)(c.position.x+c.center.x)/zone_size), colliders.length-1);
        int zone_y = Math.min(Math.max(0, (int)(c.position.y+c.center.x)/zone_size), colliders[0].length-1);
        colliders[zone_x][zone_y].add(c);
    }

    private static void removeFromCollidersArray(Collider c) {
        int zone_x = Math.min(Math.max(0, (int)(c.position.x+c.center.x)/zone_size), colliders.length-1);
        int zone_y = Math.min(Math.max(0, (int)(c.position.y+c.center.x)/zone_size), colliders[0].length-1);
        colliders[zone_x][zone_y].remove(c);
    }

    private static ArrayList<Collider> getCollidersInZone(int x, int y) {
        if(x >= 0 && x < colliders.length && y >= 0 && y < colliders[0].length) {
            return colliders[x][y];
        }
        else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<Collider> getCollidersInNeighboringZones(double x, double y) {
        ArrayList<Collider> colliders = new ArrayList<>();

        int zone_x = (int)(x/zone_size);
        int zone_y = (int)(y/zone_size);

        colliders.addAll(getCollidersInZone(zone_x, zone_y));

        // Cardinal directions
        colliders.addAll(getCollidersInZone(zone_x-1, zone_y));
        colliders.addAll(getCollidersInZone(zone_x, zone_y-1));
        colliders.addAll(getCollidersInZone(zone_x+1, zone_y));
        colliders.addAll(getCollidersInZone(zone_x, zone_y+1));

        // Diagonals
        colliders.addAll(getCollidersInZone(zone_x-1, zone_y-1));
        colliders.addAll(getCollidersInZone(zone_x-1, zone_y+1));
        colliders.addAll(getCollidersInZone(zone_x+1, zone_y-1));
        colliders.addAll(getCollidersInZone(zone_x+1, zone_y+1));

        return colliders;
    }

    /* Collision type */

    /**
     * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
     */
    public static class Collision {
        public Collider collider;
        public boolean collision_found;
        public PhysicsObject collided_with;
        public ArrayList<Vector2> intersections = new ArrayList<>();
        public Vector2 normal_reject;
        public Vector2 to_contact;

        public Collision(Collider collider) {
            this.collider = collider;
            this.collision_found = false;
        }
    }

    /* Instantiable Collider class */

    protected PhysicsObject object; // The object this collider is attached to
    protected Vector2 position; // The coordinates of the top left corner of this collider in the game world
    protected Vector2 center; // Center point of the collider in local space; initially set to the mean of the vertices
    private final ArrayList<Vector2> local_vertices = new ArrayList<>(); // Vertices in local space
    public boolean draw_self = false;
    private Collision last_collision; // Used for drawing

    /**
     * If true, the collider checks for collisions every frame and generates collision events. See
     * {@code PhysicsObject.collisionEvent}.
     * @see PhysicsObject
     */
    public boolean active_check = false;

    /* Constructors */

    /**
     * @param object The {@code PhysicsObject} to which to attach.
     * @param local_vertices A list of vertices in clockwise order.
     */
    public Collider(PhysicsObject object, Vector2[] local_vertices) {
        super(object.priority, object.layer);
        this.object = object;
        this.position = Vector2.zero();
        this.local_vertices.addAll(Arrays.asList(local_vertices));
        this.center = Vector2.zero();
        for(Vector2 v : this.local_vertices) {
            this.center = this.center.sum(v);
        }
        this.center = this.center.multiply(1.0/this.local_vertices.size());
        addToCollidersArray(this);
    }

    /**
     * @param object The {@code PhysicsObject} to which to attach.
     * @param x_offset {@code X} offset of the top left corner.
     * @param y_offset {@code Y} offset of the top left corner.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     * @return A new Collider in the shape of a rectangle.
     */
    public static Collider newBox(PhysicsObject object, double x_offset, double y_offset, double width, double height) {
        Vector2[] vertices = {new Vector2(x_offset, y_offset),
                new Vector2(x_offset+width-edge_separation, y_offset),
                new Vector2(x_offset+width-edge_separation, y_offset+height-edge_separation),
                new Vector2(x_offset, y_offset+height-edge_separation)};
        Collider collider = new Collider(object, vertices);
        collider.setPosition(object.position);
        return collider;
    }

    public static Collider newPolygon(PhysicsObject object, int num_sides, double x_offset, double y_offset,
                                      double radius, double rotation) {
        if(num_sides < 3) {
            return null;
        }

        Vector2[] vertices = new Vector2 [num_sides];
        double rotation_step = 2*Math.PI/num_sides;
        rotation += rotation_step/2;
        radius -= edge_separation;
        for(int i = 0;i < num_sides;i++) {
            rotation += rotation_step;
            vertices[i] = new Vector2(x_offset + radius + radius*Math.cos(rotation),
                                      y_offset + radius + radius*Math.sin(rotation));
        }
        Collider collider = new Collider(object, vertices);
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
        removeFromCollidersArray(this);
        this.position = position.copy();
        addToCollidersArray(this);
    }

    /**
     * @return The center of this {@code Collider} in global space.
     */
    public Vector2 getCenter() {
        return center.sum(position);
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
        if(!this.isDeleted()) {
            super.delete();
            removeFromCollidersArray(this);
            this.object = null;
        }
    }

    /**
     * @return A list of the positions of the collider's vertices in global space.
     */
    public ArrayList<Vector2> getVertices() {
        ArrayList<Vector2> vertices = new ArrayList<>();
        for(Vector2 v : local_vertices) {
            vertices.add(v.sum(position));
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
     * Checks which {@code Colliders} {@code this} collides with at its current position. Checks all other
     * {@code Colliders} in the game world.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions() {
        return getCollisions(getCollidersInNeighboringZones(position.x, position.y));
    }

    /**
     * Checks which {@code Colliders} {@code this} collides with at its current position.
     * @param colliders A list of {@code Colliders} to check.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions(ArrayList<Collider> colliders) {
        ArrayList<Collision> collisions = new ArrayList<>();

        for(Collider c : colliders) {
            if(c != this) {
                Collision collision = this.getIntersections(c);
                if(collision.collision_found) {
                    collisions.add(collision);
                }
            }
        }

        return collisions;
    }

    /**
     * Checks if {@code this} collides with other. This function should be overridden to handle collisions with
     * different collider types.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public Collision getIntersections(Collider other) {
        Collision collision = new Collision(this);

        for(Line this_l : this.getLines()) {
            for(Line other_l : other.getLines()) {
                Vector2 p = this_l.intersection(other_l);
                if(p != null) {
                    collision.collision_found = true;
                    collision.collided_with = other.object;
                    Misc.addNoDuplicates(collision.intersections, p);
                }
            }
        }

        last_collision = collision;

        return collision;
    }

    /**
     * Returns the proper normal vector for the first collision experienced by {@code this} when moving from
     * {@code this.position} to {@code this.position + delta_position}. Collisions are only checked for the provided
     * list of colliders.
     *
     * @param delta_position The new position after the desired translation.
     * @param colliders A set of colliders to check.
     * @return A {@code Collision} encapsulating the first collision experienced when moving by delta_position.
     * Populates {@code collision_found}, {@code collided_with}, {@code intersections}, {@code normal},
     */
    public Collision getCollisionDetails(Vector2 delta_position, ArrayList<Collider> colliders) {
        double closest_distance = Double.MAX_VALUE;
        Line closest_ray = null;
        Vector2 closest_intersection = null;
        Line closest_edge = null;
        Collider closest_collider = null;

        // Sweep the corners of this collider across delta_position
        for(Vector2 corner : this.getVertices()) {

            // Get the line from this corner's starting point to its ending point
            Line ray = new Line(corner, corner.sum(delta_position));

            // Check for intersections with all edges of other colliders
            for(Collider other : colliders) {
                for(Line edge : other.getLines()) {
                    Vector2 intersection = ray.intersection(edge);

                    // Record the closest intersection
                    if(intersection != null) {
                        double length_from_start = intersection.difference(corner).abs();
                        if(length_from_start < closest_distance) {
                            closest_ray = ray;
                            closest_intersection = intersection;
                            closest_edge = edge;
                            closest_distance = length_from_start;
                            closest_collider = other;
                        }
                    }
                }
            }
        }

        // Sweep the corners of the other colliders across -1*delta_position
        for(Collider other : colliders) {
            for (Vector2 corner : other.getVertices()) {

                // Get the line from the other's corner's starting point to its ending point (relative to this)
                Line ray = new Line(corner, corner.difference(delta_position));

                // Check for intersections with this collider's edges
                for (Line edge : this.getLines()) {
                    Vector2 intersection = ray.intersection(edge);

                    // Record the closest intersection
                    if (intersection != null) {
                        double length_from_start = intersection.difference(corner).abs();
                        if (length_from_start < closest_distance) {
                            closest_ray = ray;
                            closest_intersection = intersection;
                            // Reverse the direction of self edges so the normal points inwards
                            closest_edge = edge.reverse();
                            closest_distance = length_from_start;
                            closest_collider = other;
                        }
                    }
                }
            }
        }

        // Return a Collision
        Collision collision = new Collision(this);

        if(closest_edge != null) {
            collision.collision_found = true;
            collision.intersections.add(closest_intersection);
            collision.collided_with = closest_collider.object;

            // Find normal
            Vector2 normal = closest_edge.RHNormal();

            // Scale normal by distance of overlap plus safety margin to find rejection vector
            Vector2 corner_to_intersection = closest_intersection.difference(closest_ray.p2);
            Vector2 proj_edge = corner_to_intersection.projection(closest_edge.vector());
            double normal_mag = (corner_to_intersection.difference(proj_edge)).abs() + reject_separation;
            collision.normal_reject = normal.multiply(normal_mag);

            // Find contact vector
            collision.to_contact = closest_intersection.difference(closest_ray.p1);
            collision.to_contact = collision.to_contact.normalize().
                    multiply(collision.to_contact.abs() - reject_separation);
        }

        return collision;
    }

    /**
     * Returns the proper normal vector for the first collision experienced by {@code this} when moving from
     * {@code this.position} to {@code this.position + delta_position}.
     *
     * @param delta_position The new position after the desired translation.
     * @return The normal vector corresponding to the first collision experienced when moving by delta_position.
     */
    public Collision getCollisionDetails(Vector2 delta_position) {
        return getCollisionDetails(delta_position, getCollidersInNeighboringZones(position.x, position.y));
    }

    @Override
    public void update() {
        if(active_check) {
            setPosition(object.position);
            ArrayList<Collision> collisions = getCollisions();
            for (Collision c : collisions) {
                if(c.collision_found) {
                    object.collisionEvent(c.collided_with);
                }
            }
        }
    }

    /**
     * Draws the edges of the collider as well as any recorded intersections from collision checks.
     */
    @Override
    public void draw() {
        if(draw_self) {
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
