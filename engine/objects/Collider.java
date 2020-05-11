package engine.objects;

import engine.GameGraphics;
import engine.World;
import engine.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class used to check for collisions between physics objects. All colliders are represented as a polygon made from n
 * points connected by n lines.
 *
 * @author Galen Savidge
 * @version 5/9/2020
 */
public class Collider extends GameObject {

    /* Class constants */

    private static int zone_size; // In pixels
    protected static ArrayList<Collider>[][] colliders; // A list of all colliders that exist

    public static final double edge_separation = 10*Misc.delta;
    public static final double reject_separation = 2*Misc.delta;


    /* Static methods */

    /**
     * Initializes the data structure used to check which {@link Collider} instances lie within a specific area. Should
     * be called before any {@link Collider} objects are created.
     * @param zone_size The size, in units of {@link engine.World}.{@code getGridScale()}.
     * @throws ExceptionInInitializerError Throws if {@link engine.World} has not yet been initialized.
     */
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


    /* Collider zone helper functions */

    private static void getZone(Collider c) {

    }

    /**
     * Adds {@code c} to the array used by the {@link #Collider} class to find nearby {@code Colliders}.
     * @see #getCollidersInZone
     */
    private static void addToCollidersArray(Collider c) {
        for(Vector2 p : c.zone_points) {
            int x = (int) ((p.x + c.position.x) / zone_size);
            int y = (int) ((p.y + c.position.y) / zone_size);
            int zone_x = Math.min(Math.max(0, x), colliders.length - 1);
            int zone_y = Math.min(Math.max(0, y), colliders[0].length - 1);
            if(!colliders[zone_x][zone_y].contains(c)) {
                colliders[zone_x][zone_y].add(c);
            }
        }
    }

    /**
     * Removes {@code c} from the array used by the {@link #Collider} class to find nearby {@code Colliders}.
     * @see #getCollidersInZone
     */
    private static void removeFromCollidersArray(Collider c) {
        for(Vector2 p : c.zone_points) {
            int x = (int) ((p.x + c.position.x) / zone_size);
            int y = (int) ((p.y + c.position.y) / zone_size);
            int zone_x = Math.min(Math.max(0, x), colliders.length - 1);
            int zone_y = Math.min(Math.max(0, y), colliders[0].length - 1);
            colliders[zone_x][zone_y].remove(c);
        }
    }

    /**
     * @return The colliders in zone {@code (x, y)} in the 2D array of zones.
     */
    private static ArrayList<Collider> getCollidersInZone(int x, int y) {
        if(x >= 0 && x < colliders.length && y >= 0 && y < colliders[0].length) {
            return colliders[x][y];
        }
        else {
            return new ArrayList<>();
        }
    }


    /* Collision class */

    /**
     * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
     */
    public static class Collision {
        public Collider collider;
        public boolean collision_found;
        public PhysicsObject collided_with;
        public ArrayList<Vector2> intersections;
        public Vector2 normal_reject;
        public Vector2 to_contact;

        public Collision(Collider collider) {
            this.collider = collider;
            this.collision_found = false;
            this.intersections = new ArrayList<>();
        }

        public Collision copy() {
            Collision c = new Collision(collider);
            c.collision_found = collision_found;
            c.collided_with = collided_with;
            c.intersections = new ArrayList<>();
            for(Vector2 i : intersections) {
                c.intersections.add(i.copy());
            }
            if(this.isDetailed()) {
                c.normal_reject = normal_reject.copy();
                c.to_contact = to_contact.copy();
            }
            return c;
        }

        public boolean isDetailed() {
            return normal_reject != null && to_contact != null;
        }
    }


    /* Collider instance variables */

    protected PhysicsObject object; // The object this collider is attached to
    protected Vector2 position; // The coordinates of the top left corner of this collider in the game world
    protected Vector2 center; // Center point of the collider in local space; initially set to the mean of the vertices
    private final ArrayList<Vector2> local_vertices = new ArrayList<>(); // Vertices in local space
    private final ArrayList<Vector2> zone_points = new ArrayList<>(); // Points used to check which zone this is in
    private boolean enabled = true; // If false, does not check for or return collisions with other Colliders
    public boolean draw_self = false;

    /**
     * If true, the collider checks for collisions every frame and generates collision events. See
     * {@code PhysicsObject.collisionEvent}.
     * @see PhysicsObject
     */
    public boolean active_check = false;

    /* Constructors */

    /**
     * @param object The {@link PhysicsObject} to which to attach.
     * @param local_vertices A list of vertices in clockwise order.
     */
    public Collider(PhysicsObject object, Vector2[] local_vertices) {
        super(object.priority, object.layer);
        this.suspend_tier = object.suspend_tier;
        this.object = object;
        this.position = Vector2.zero();
        this.local_vertices.addAll(Arrays.asList(local_vertices));
        this.center = Vector2.zero();
        for(Vector2 v : this.local_vertices) {
            this.center = this.center.sum(v);
        }
        this.center = this.center.multiply(1.0/this.local_vertices.size());

        // Get zone check points list
        this.zone_points.add(this.center);
        for(Line l : this.getLines(false)) {
            if(l.length() >= zone_size || l.p1.difference(this.center).abs() >= zone_size) {
                Vector2 new_point = l.p1;
                while((new_point.difference(l.p1)).abs() < l.length()) {
                    this.zone_points.add(new_point.copy());
                    new_point = new_point.sum(l.vector().normalize().multiply(zone_size));
                }
            }
        }

        addToCollidersArray(this);
    }

    /**
     * @param object The {@link PhysicsObject} to which to attach.
     * @param x_offset {@code X} offset of the top left corner.
     * @param y_offset {@code Y} offset of the top left corner.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     * @return A new {@link Collider} in the shape of a rectangle.
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

    /**
     * @param object The {@link PhysicsObject} to which to attach.
     * @param num_sides The number of sides of the polygon.
     * @param x_offset {@code X} offset of the top left corner.
     * @param y_offset {@code Y} offset of the top left corner.
     * @param radius The distance from the center of the polygon to each corner.
     * @param rotation Rotation in radians. With no rotation the top face of the polygon is horizontal.
     * @return A new {@link Collider} in the shape of a regular polygon.
     */
    public static Collider newPolygon(PhysicsObject object, int num_sides, double x_offset, double y_offset,
                                      double radius, double rotation) {

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
     * @return The position of the top left corner of the {@link Collider}'s bounding box in world space.
     */
    public Vector2 getPosition() {
        return position.copy();
    }

    /**
     * @param position The position of the top left corner of the {@link Collider}'s bounding box in world space.
     */
    public void setPosition(Vector2 position) {
        removeFromCollidersArray(this);
        this.position = position.copy();
        addToCollidersArray(this);
    }

    /**
     * @return The center of this {@link Collider} in world space.
     */
    public Vector2 getCenter() {
        return center.sum(position);
    }

    /**
     * @param center The new center for this {@link Collider} in local space.
     */
    public void setCenter(Vector2 center) {
        this.center = center.copy();
    }

    /**
     * Disables the {@code Collider}, meaning it will no longer actively check for collisions and will not be returned
     * by collision checks by other {@code Colliders}. {@code Colliders} are enabled by default.
     */
    public void disable() {
        this.enabled = false;
        Collider.removeFromCollidersArray(this);
    }

    /**
     * Enables the {@link Collider} if it is disabled.
     * @see #disable()
     */
    public void enable() {
        this.enabled = true;
        Collider.addToCollidersArray(this);
    }

    /**
     * @return True iff this {@link Collider} is enabled.
     * @see #disable()
     */
    public boolean isEnabled() {
        return this.enabled;
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
    public ArrayList<Line> getLines(boolean global) {
        ArrayList<Vector2> vertices;
        if(global) {
            vertices = this.getVertices();
        }
        else {
            vertices = this.local_vertices;
        }
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0;i < vertices.size();i++) {
            int j = i-1;
            if(j < 0) j += vertices.size();
            lines.add(new Line(vertices.get(j), vertices.get(i)));
        }
        return lines;
    }

    /**
     * @return A list of the line segments connecting the collider's vertices running clockwise.
     */
    public ArrayList<Line> getLines() {
        return getLines(true);
    }

    /**
     * @return A list of nearby {@link Collider} objects. Objects returned lie in the same zone as or neighboring zones
     * to this {@link Collider}, where zone size is defined by {@link #initColliders}.
     */
    public ArrayList<Collider> getCollidersInNeighboringZones() {

        int zone_x = (int)((position.x + center.x)/zone_size);
        int zone_y = (int)((position.y + center.x)/zone_size);

        ArrayList<Collider> colliders = new ArrayList<>(getCollidersInZone(zone_x, zone_y));
        colliders.remove(this);

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

    /**
     * Checks which {@code Colliders} {@code this} collides with at its current position. Checks all other
     * {@code Colliders} in the game world.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions() {
        return getCollisions(this.getCollidersInNeighboringZones());
    }

    /**
     * Checks which {@code Colliders} {@code this} collides with at its current position.
     * @param colliders A list of {@code Colliders} to check.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions(ArrayList<Collider> colliders) {
        ArrayList<Collision> collisions = new ArrayList<>();

        if(this.enabled) {
            for (Collider c : colliders) {
                if (c != this) {
                    Collision collision = this.getIntersections(c);
                    if (collision.collision_found) {
                        collisions.add(collision);
                    }
                }
            }
        }

        return collisions;
    }

    /**
     * Checks if {@code this} collides with {@code other}.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    private Collision getIntersections(Collider other) {

        Collision collision = new Collision(this);

        for (Line this_l : this.getLines()) {
            for (Line other_l : other.getLines()) {
                Vector2 p = this_l.intersection(other_l);
                if (p != null) {
                    collision.collision_found = true;
                    collision.collided_with = other.object;
                    Misc.addNoDuplicates(collision.intersections, p);
                }
            }
        }

        return collision;
    }

    /**
     * Returns the proper normal vector for the first collision experienced by {@code this} when moving from
     * {@code this.position} to {@code this.position + delta_position}. Collisions are only checked for the provided
     * list of colliders.
     *
     * @param delta_position The new position after the desired translation.
     * @param colliders A set of colliders to check.
     * @return A {@link Collision} encapsulating the first collision experienced when moving by delta_position.
     * Populates {@code collision_found}, {@code collided_with}, {@code intersections}, {@code normal},
     */
    public Collision getCollisionDetails(Vector2 delta_position, ArrayList<Collider> colliders) {
        if(!this.enabled || colliders.size() == 0) {
            return new Collision(this);
        }

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
            collision.to_contact = delta_position.normalize().multiply(closest_distance - reject_separation);
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
        return getCollisionDetails(delta_position, this.getCollidersInNeighboringZones());
    }

    /**
     * Runs active checks for intersections with other colliders, if enabled.
     */
    @Override
    public void update() {
        if(this.enabled && active_check) {
            setPosition(object.position);
            ArrayList<Collision> collisions = getCollisions();
            for (Collision c : collisions) {
                if(c.collision_found) {
                    object.intersectionEvent(c);
                    Collision other_c = c.copy();
                    other_c.collided_with = object;
                    c.collided_with.intersectionEvent(other_c);
                }
            }
        }
    }

    /**
     * Draws the edges and center of the collider.
     */
    @Override
    public void draw() {
        if(draw_self) {
            GameGraphics.drawPoint((int)this.getCenter().x,(int)this.getCenter().y, false, Color.black);

            ArrayList<Line> lines = getLines();
            for(Line l : lines) {
                l.draw();
            }

            for(Vector2 p : zone_points) {
                GameGraphics.drawPoint((int)(position.x + p.x), (int)(position.y + p.y), false, Color.RED);
            }
        }
    }
}
