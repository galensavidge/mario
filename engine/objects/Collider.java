package engine.objects;

import engine.GameGraphics;
import engine.World;
import engine.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A class used to check for collisions between physics objects. All colliders are represented as a polygon made from n
 * points connected by n lines.
 *
 * @author Galen Savidge
 * @version 5/17/2020
 */
public class Collider extends GameObject {

    /* Class constants */

    private static final int zone_size_in_grid = 2; // Zone size in grid squares
    private static int zone_size; // Zone size in pixels
    protected static ArrayList<Collider>[][] colliders; // A list of all colliders that exist as a 2D array of zones

    public static final double edge_separation = 10*Misc.delta;
    public static final double reject_separation = 2*Misc.delta;


    /* Static methods */

    /**
     * Initializes the data structure used to check which {@link Collider} instances lie within a specific area. Should
     * be called before any {@link Collider} objects are created.
     *
     * @throws ExceptionInInitializerError Throws if {@link engine.World} has not yet been initialized.
     */
    public static void initColliders() throws ExceptionInInitializerError {
        if(World.getGridSize() == 0) {
            throw new ExceptionInInitializerError("World not initialized!");
        }

        Collider.zone_size = zone_size_in_grid*World.getGridSize();
        int grid_width = World.getWidth()/zone_size + 1;
        int grid_height = World.getHeight()/zone_size + 1;
        colliders = new ArrayList[grid_width][grid_height];
        for(int i = 0;i < grid_width;i++) {
            for(int j = 0;j < grid_height;j++) {
                colliders[i][j] = new ArrayList<>();
            }
        }
    }

    /**
     * Ray-casts from a given {@code position} vector along the {@code direction} vector and checks for intersections
     * with a given set of {@link Collider} objects. Returns the collision details for the intersection closest to
     * {@code position}.
     */
    public static Collision rayCast(Vector2 position, Vector2 direction, ArrayList<Collider> colliders) {
        Line ray = new Line(position.copy(), position.sum(direction));

        double closest_distance = Double.MAX_VALUE;
        Vector2 closest_intersection = null;
        Line closest_edge = null;
        Collider closest_collider = null;


        for(Collider collider : colliders) {
            ArrayList<Line> edges = collider.getEdges();
            for(Line edge : edges) {
                Vector2 intersection = ray.intersection(edge);
                if(intersection != null) {
                    if(intersection.difference(position).abs() < closest_distance) {
                        closest_intersection = intersection;
                        closest_edge = edge;
                        closest_collider = collider;
                    }
                }
            }
        }

        Collision collision = new Collision();
        if(closest_intersection != null) {
            collision.collision_found = true;
            collision.collided_with = closest_collider.object;
            collision.intersections.add(closest_intersection);

            // Find normal
            Vector2 normal = closest_edge.RHNormal();

            // Scale normal by distance of overlap plus safety margin to find rejection vector
            Vector2 corner_to_intersection = closest_intersection.difference(ray.p2);
            Vector2 proj_edge = corner_to_intersection.projection(closest_edge.vector());
            double normal_mag = (corner_to_intersection.difference(proj_edge)).abs() + reject_separation;
            collision.normal_reject = normal.multiply(normal_mag);

            // Find contact vector
            collision.to_contact = ray.vector().normalize().multiply(closest_distance - reject_separation);
        }

        return collision;
    }


    /* Collision class */

    /**
     * This class holds information about collision events. It is instantiated by certain {@code Collider} methods.
     */
    public static class Collision {
        /**
         * True if at least one intersection was found.
         */
        public boolean collision_found;

        /**
         * The object to which the other {@link Collider} belongs, if applicable. If {@code collision_found = false},
         * this will be {@code null}.
         */
        public PhysicsObject collided_with;

        /**
         * The list of intersection points founds at this collision.
         */
        public ArrayList<Vector2> intersections;

        /**
         * Returned when doing a sweep or ray-cast: the change in position required to fully resolve the collision so
         * that the objects no longer collide. Normal to the first surface collided with.
         */
        public Vector2 normal_reject;

        /**
         * Returned when doing a sweep or ray-cast: the change in position to move to the point of contact with the
         * other object. Parallel to the direction of the sweep or ray-cast.
         */
        public Vector2 to_contact;

        /**
         * Creates an empty {@link Collision} instance where {@code collision_found = false} and {@code intersections}
         * is an empty list.
         */
        public Collision() {
            this.collision_found = false;
            this.intersections = new ArrayList<>();
        }

        /**
         * @return A copy of this {@link Collision} with new copies of its contained data structures.
         */
        public Collision copy() {
            Collision c = new Collision();
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

        /**
         * @return True if the {@link Collision} has collision details, i.e. was returned by {@code
         * Collider.getCollisionDetails}. If false, the {@code normal_reject} and {@code to_contact} vectors are {@code
         * null}.
         */
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

    /**
     * If true, the {@link Collider} draws its vertices in its draw step.
     */
    public boolean draw_self = false;

    /**
     * If true, the {@link Collider} checks for intersections every frame and generates intersection events. See {@code
     * PhysicsObject.intersectionEvent}. Defaults to {@code false}.
     *
     * @see PhysicsObject
     */
    public boolean active_check = false;


    /* Constructors and destructors */

    /**
     * @param object         The {@link PhysicsObject} to which to attach.
     * @param local_vertices A list of vertices where each adjacent pair forms one edge of the polygon.
     */
    public Collider(PhysicsObject object, Vector2[] local_vertices) {
        super(object.priority, object.layer);
        this.suspend_tier = object.suspend_tier;
        this.object = object;

        // Add vertices in clockwise order
        this.local_vertices.addAll(Arrays.asList(local_vertices));
        if(polygonIsCCW(local_vertices)) {
            Collections.reverse(this.local_vertices);
        }

        // Calculate center
        this.center = Vector2.zero();
        for(Vector2 v : this.local_vertices) {
            this.center = this.center.sum(v);
        }
        this.center = this.center.multiply(1.0/this.local_vertices.size());

        // Get zone check points list
        this.zone_points.add(this.center);
        this.position = Vector2.zero();
        for(Line l : this.getEdges(false)) {
            if(l.length() >= zone_size || l.p1.difference(this.center).abs() >= zone_size) {
                Vector2 new_point = l.p1;
                while((new_point.difference(l.p1)).abs() < l.length()) {
                    this.zone_points.add(new_point.copy());
                    new_point = new_point.sum(l.vector().normalize().multiply(zone_size));
                }
            }
        }

        // Set position and add to colliders array
        this.setPosition(object.position);
    }

    /**
     * Makes a rectangular {@link Collider} with the given proportions. Shrinks the rectangle by {@code
     * Collider.edge_separation} on each side.
     *
     * @param object   The {@link PhysicsObject} to which to attach.
     * @param x_offset Xoffset of the top left corner.
     * @param y_offset Y offset of the top left corner.
     * @param width    Width of the rectangle.
     * @param height   Height of the rectangle.
     * @return A new {@link Collider} in the shape of a rectangle.
     */
    public static Collider newBox(PhysicsObject object, double x_offset, double y_offset, double width, double height) {
        Vector2[] vertices = {new Vector2(x_offset, y_offset),
                new Vector2(x_offset + width - edge_separation, y_offset),
                new Vector2(x_offset + width - edge_separation, y_offset + height - edge_separation),
                new Vector2(x_offset, y_offset + height - edge_separation)};

        return new Collider(object, vertices);
    }

    /**
     * Makes a {@link Collider} in the shape of a regular polygon. Reduces the distance to each vertex by {@code
     * Collider.edge_separation}.
     *
     * @param object    The {@link PhysicsObject} to which to attach.
     * @param num_sides The number of sides of the polygon.
     * @param x_offset  X offset of the top left corner.
     * @param y_offset  Y offset of the top left corner.
     * @param radius    The distance from the center of the polygon to each corner.
     * @param rotation  Rotation in radians. With no rotation the top face of the polygon is horizontal.
     * @return A new {@link Collider} in the shape of a regular polygon.
     */
    public static Collider newPolygon(PhysicsObject object, int num_sides, double x_offset, double y_offset,
                                      double radius, double rotation) {

        Vector2[] vertices = new Vector2[num_sides];
        double rotation_step = 2*Math.PI/num_sides;
        rotation += rotation_step/2;
        radius -= edge_separation;
        for(int i = 0;i < num_sides;i++) {
            rotation += rotation_step;
            vertices[i] = new Vector2(x_offset + radius + radius*Math.cos(rotation),
                    y_offset + radius + radius*Math.sin(rotation));
        }

        return new Collider(object, vertices);
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


    /* Instance methods */

    /**
     * @return The position of the top left corner of the {@link Collider}'s bounding box in world space.
     */
    public Vector2 getPosition() {
        return position.copy();
    }

    /**
     * Moves the {@link Collider} and
     *
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
        if(this.enabled) {
            this.enabled = false;
            Collider.removeFromCollidersArray(this);
        }
    }

    /**
     * Enables the {@link Collider} if it is disabled.
     *
     * @see #disable()
     */
    public void enable() {
        if(!this.enabled) {
            this.enabled = true;
            Collider.addToCollidersArray(this);
        }
    }

    /**
     * @return True iff this {@link Collider} is enabled.
     * @see #disable()
     */
    public boolean isEnabled() {
        return this.enabled;
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
     * @param global True to use world coordinates, false to use local coordinates.
     * @return A list of the line segments connecting the collider's vertices running clockwise.
     */
    public ArrayList<Line> getEdges(boolean global) {
        ArrayList<Vector2> vertices;
        if(global) {
            vertices = this.getVertices();
        }
        else {
            vertices = this.local_vertices;
        }
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0;i < vertices.size();i++) {
            int j = i - 1;
            if(j < 0) j += vertices.size();
            lines.add(new Line(vertices.get(j), vertices.get(i)));
        }
        return lines;
    }

    /**
     * @return A list of the line segments connecting the collider's vertices running clockwise, using world
     * coordinates.
     */
    public ArrayList<Line> getEdges() {
        return getEdges(true);
    }

    /**
     * @param distance Number of zones away in every direction (including diagonals) to check.
     * @return A list of nearby {@link Collider} objects. Objects returned lie in the same zone as or neighboring zones
     * to this {@link Collider}, where zone size is defined by {@link #initColliders}. This object is excluded.
     */
    public ArrayList<Collider> getCollidersInNeighboringZones(int distance) {

        int zone_x = (int)((position.x + center.x)/zone_size);
        int zone_y = (int)((position.y + center.x)/zone_size);

        ArrayList<Collider> colliders = new ArrayList<>();

        for(int i = -distance;i <= distance;i++) {
            for(int j = -distance;j <= distance;j++) {
                ArrayList<Collider> zone = getCollidersInZone(zone_x + i, zone_y + j);
                zone.removeAll(colliders);
                colliders.addAll(zone);
            }
        }
        colliders.remove(this);

        return colliders;
    }

    /**
     * @return A list of nearby {@link Collider} objects. Objects returned lie in the same zone as or neighboring zones
     * to this {@link Collider}, where zone size is defined by {@link #initColliders}.
     */
    public ArrayList<Collider> getCollidersInNeighboringZones() {
        return getCollidersInNeighboringZones(1);
    }

    /**
     * Returns the set of points at which a given {@link Line} intersects the edges of this {@link Collider}.
     */
    public static ArrayList<Vector2> lineIntersectsCollider(Line line, Collider collider) {
        ArrayList<Vector2> intersections = new ArrayList<>();
        for(Line other_l : collider.getEdges()) {
            Vector2 p = line.intersection(other_l);
            if(p != null) {
                intersections.add(p);
            }
        }
        return intersections;
    }

    /**
     * Checks which {@code Colliders} {@code this} collides with at its current position. Checks all other {@code
     * Colliders} in the game world.
     *
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions() {
        return getCollisions(this.getCollidersInNeighboringZones());
    }

    /**
     * Checks which {@code Colliders} {@code this} collides with at its current position.
     *
     * @param colliders A list of {@code Colliders} to check.
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    public ArrayList<Collision> getCollisions(ArrayList<Collider> colliders) {
        ArrayList<Collision> collisions = new ArrayList<>();

        if(this.enabled) {
            for(Collider c : colliders) {
                if(c != this) {
                    Collision collision = this.getIntersections(c);
                    if(collision.collision_found) {
                        collisions.add(collision);
                    }
                }
            }
        }

        return collisions;
    }

    /**
     * Returns the proper normal vector for the first collision experienced by {@code this} when moving from {@code
     * this.position} to {@code this.position + delta_position}. Collisions are only checked for the provided list of
     * colliders.
     *
     * @param delta_position The new position after the desired translation.
     * @param colliders      A set of colliders to check.
     * @return A {@link Collision} encapsulating the first collision experienced when moving by delta_position.
     * Populates {@code collision_found}, {@code collided_with}, {@code intersections}, {@code normal},
     */
    public Collision getCollisionDetails(Vector2 delta_position, ArrayList<Collider> colliders) {
        if(!this.enabled || colliders.size() == 0) {
            return new Collision();
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
                for(Line edge : other.getEdges()) {
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
            for(Vector2 corner : other.getVertices()) {

                // Get the line from the other's corner's starting point to its ending point (relative to this)
                Line ray = new Line(corner, corner.difference(delta_position));

                // Check for intersections with this collider's edges
                for(Line edge : this.getEdges()) {
                    Vector2 intersection = ray.intersection(edge);

                    // Record the closest intersection
                    if(intersection != null) {
                        double length_from_start = intersection.difference(corner).abs();
                        if(length_from_start < closest_distance) {
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
        Collision collision = new Collision();

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
     * Returns the proper normal vector for the first collision experienced by {@code this} when moving from {@code
     * this.position} to {@code this.position + delta_position}.
     *
     * @param delta_position The new position after the desired translation.
     * @return The normal vector corresponding to the first collision experienced when moving by delta_position.
     */
    public Collision getCollisionDetails(Vector2 delta_position) {
        return getCollisionDetails(delta_position, this.getCollidersInNeighboringZones());
    }


    /* Events */

    /**
     * Runs active checks for intersections with other colliders, if enabled.
     */
    @Override
    public void update() {
        if(this.enabled && active_check) {
            setPosition(object.position);
            ArrayList<Collision> collisions = getCollisions();
            for(Collision c : collisions) {
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
        if(enabled && draw_self) {
            GameGraphics.drawPoint((int)this.getCenter().x, (int)this.getCenter().y, false, Color.black);

            ArrayList<Line> lines = getEdges();
            for(Line l : lines) {
                l.draw();
            }

            for(Vector2 p : zone_points) {
                GameGraphics.drawPoint((int)(position.x + p.x), (int)(position.y + p.y), false, Color.RED);
            }
        }
    }


    /* Helper functions */

    /**
     * @param vertices A list of vertices in either clockwise or counter-clockwise order.
     * @return {@code true} if the vertices are in counter-clockwise order.
     */
    private boolean polygonIsCCW(Vector2[] vertices) {
        double net_cw_angle = 0;
        for(int i = 0;i < vertices.length;i++) {
            Vector2 v1 = getVertex(vertices, i).difference(getVertex(vertices, i - 1));
            Vector2 v2 = getVertex(vertices, i + 1).difference(getVertex(vertices, i));
            double vertex_cw_angle = v2.clockwiseAngle() - v1.clockwiseAngle();
            if(vertex_cw_angle < 0) {
                vertex_cw_angle += Math.PI*2;
            }
            net_cw_angle += vertex_cw_angle;
        }
        return net_cw_angle != Math.PI*2;
    }

    /**
     * Helper function to index into a circular list of vertices using any positive or negative integer.
     */
    private Vector2 getVertex(Vector2[] vertices, int index) {
        while(index >= vertices.length) {
            index -= vertices.length;
        }
        while(index < 0) {
            index += vertices.length;
        }
        return vertices[index];
    }

    /**
     * Checks if {@code this} collides with {@code other}.
     *
     * @return A Collision object. Populates {@code collision_found}, {@code collided_with}, and {@code intersections}.
     */
    private Collision getIntersections(Collider other) {

        Collision collision = new Collision();

        for(Line this_l : this.getEdges()) {
            ArrayList<Vector2> intersections = lineIntersectsCollider(this_l, other);
            if(intersections.size() > 0) {
                collision.collision_found = true;
                collision.collided_with = other.object;
                Misc.addNoDuplicates(collision.intersections, intersections);
            }
        }

        return collision;
    }


    /* Collider zone helper functions */

    /**
     * Adds {@code c} to the array used by the {@link #Collider} class to find nearby {@code Colliders}.
     *
     * @see #getCollidersInZone
     */
    private static void addToCollidersArray(Collider c) {
        for(Vector2 p : c.zone_points) {
            int x = (int)((p.x + c.position.x)/zone_size);
            int y = (int)((p.y + c.position.y)/zone_size);
            int zone_x = Math.min(Math.max(0, x), colliders.length - 1);
            int zone_y = Math.min(Math.max(0, y), colliders[0].length - 1);
            if(!colliders[zone_x][zone_y].contains(c)) {
                colliders[zone_x][zone_y].add(c);
            }
        }
    }

    /**
     * Removes {@code c} from the array used by the {@link #Collider} class to find nearby {@code Colliders}.
     *
     * @see #getCollidersInZone
     */
    private static void removeFromCollidersArray(Collider c) {
        for(Vector2 p : c.zone_points) {
            int x = (int)((p.x + c.position.x)/zone_size);
            int y = (int)((p.y + c.position.y)/zone_size);
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
            return new ArrayList<>(colliders[x][y]);
        }
        else {
            return new ArrayList<>();
        }
    }
}
