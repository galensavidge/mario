package engine.collider;

import engine.graphics.GameGraphics;
import engine.objects.GameObject;
import engine.objects.PhysicsObject;
import engine.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * A class used to check for collisions between physics objects. All colliders are represented as a polygon made from n
 * points connected by n lines.
 *
 * @author Galen Savidge
 * @version 6/1/2020
 */
public class Collider extends GameObject {

    public static final double edge_separation = 50*Misc.delta;
    public static final double reject_separation = 10*Misc.delta;


    /* Collider instance variables */

    private PhysicsObject object; // The object this collider is attached to
    private Vector2 position; // The coordinates of the top left corner of this collider in the game world
    private Vector2 center; // The center of the collider's bounding box; can also be manually set
    private final ArrayList<Vector2> local_vertices = new ArrayList<>(); // Vertices in local space
    final ArrayList<Vector2> zone_check_points = new ArrayList<>(); // Points used to check which zone this is in
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
        super(object.getPriority(), object.getLayer());
        this.suspend_tier = object.getSuspendTier();
        this.object = object;

        // Calculate center
        double max_x = Double.MIN_VALUE, min_x = Double.MAX_VALUE, max_y = Double.MIN_VALUE, min_y = Double.MAX_VALUE;
        for(Vector2 v : local_vertices) {
            if(v.x > max_x) {
                max_x = v.x;
            }
            if(v.x < min_x) {
                min_x = v.x;
            }
            if(v.y > max_y) {
                max_y = v.y;
            }
            if(v.y < min_y) {
                min_y = v.y;
            }
        }
        this.center = new Vector2((max_x + min_x)/2.0, (max_y + min_y)/2.0);

        // Subtract edge separation from each vertex
        for(Vector2 vertex : local_vertices) {
            vertex.x += vertex.x > center.x ? -edge_separation : edge_separation;
            this.local_vertices.add(vertex);
        }

        // Make sure vertices are in clockwise order
        if(polygonIsCCW(local_vertices)) {
            Collections.reverse(this.local_vertices);
        }

        // Get zone check points list
        this.zone_check_points.add(this.center.copy());
        this.position = Vector2.zero();
        int zone_size = ColliderGrid.getZoneSize();
        for(Line l : this.getEdges(false)) {
            if(l.length() >= zone_size || l.p1.difference(this.center).abs() >= zone_size) {
                Vector2 new_point = l.p1;
                while((new_point.difference(l.p1)).abs() < l.length()) {
                    this.zone_check_points.add(new_point.copy());
                    new_point = new_point.sum(l.vector().normalize().multiply(zone_size));
                }
            }
        }

        // Set position and add to colliders array
        this.setPosition(object.getPosition());
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
            ColliderGrid.remove(this);
            this.object = null;
        }
    }

    /**
     * Disables the {@code Collider}, meaning it will no longer actively check for collisions and will not be returned
     * by collision checks by other {@code Colliders}. {@code Colliders} are enabled by default.
     */
    public void disable() {
        if(this.enabled) {
            this.enabled = false;
            ColliderGrid.remove(this);
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
            ColliderGrid.add(this);
        }
    }


    /* Accessors */

    /**
     * @return The {@link PhysicsObject} this {@link Collider} is attached to.
     */
    public PhysicsObject getObject() {
        return object;
    }

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
        ColliderGrid.remove(this);
        this.position = position.copy();
        ColliderGrid.add(this);
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
     * @return The x-distance from the origin to the rightmost vertex.
     */
    public double getWidth() {
        double right = Double.MIN_VALUE;
        for(Vector2 vertex : local_vertices) {
            if(vertex.x > right) {
                right = vertex.x;
            }
        }
        return right;
    }

    /**
     * @return The y-distance from the origin to the lowest vertex.
     */
    public double getHeight() {
        double bottom = Double.MIN_VALUE;
        for(Vector2 vertex : local_vertices) {
            if(vertex.y > bottom) {
                bottom = vertex.y;
            }
        }
        return bottom;
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


    /* Collision checking */

    /**
     * Checks for collisions at {@code position}.
     *
     * @param position Coordinates in world space.
     * @param filter   A condition defining which objects to check.
     * @return A list of {@code PhysicsObject} instances collided with.
     */
    public ArrayList<PhysicsObject> check(Vector2 position, Predicate<PhysicsObject> filter) {
        ArrayList<PhysicsObject> objects = new ArrayList<>();
        if(!enabled) {
            return objects;
        }

        ArrayList<Collider> colliders = ColliderGrid.inNeighboringZones(position);
        setPosition(position);

        for(Line edge : this.getEdges()) {
            for(Collider other : colliders) {
                if(other != this && filter.test(other.object)) {
                    boolean intersects = false;
                    for(Line other_edge : other.getEdges()) {
                        if(edge.intersection(other_edge) != null) {
                            intersects = true;
                            break;
                        }
                    }
                    if(intersects) {
                        objects.add(other.object);
                    }
                }
            }
        }
        return objects;
    }

    /**
     * Checks for collisions at {@code position}.
     *
     * @param position Coordinates in world space.
     * @return A list of {@code PhysicsObject} instances collided with.
     */
    public ArrayList<PhysicsObject> check(Vector2 position) {
        return check(position, o -> true);
    }


    /**
     * Ray-casts from a given {@code position} vector along the {@code direction} vector and checks for intersections
     * with this {@link Collider}'s edges. Adds intersections to the passed {@link Collision} instance.
     *
     * @param reversed True to reverse normals, to-contact, and reject vectors for any returned {@link Intersection}
     *                 objects.
     * @param filter   A condition defining which intersections to return.
     */
    public void rayCheck(PhysicsObject calling_obj, Collision collision, Line ray, boolean reversed,
                         Predicate<Intersection> filter) {
        ArrayList<Line> edges = this.getEdges();
        for(Line edge : edges) {
            Vector2 intersection_point = ray.intersection(edge);
            if(intersection_point != null) {
                PhysicsObject collided_with = this.object;
                if(reversed) {
                    collided_with = calling_obj;
                }
                Intersection i = new Intersection(collided_with, intersection_point, edge, ray, reversed);
                if(filter.test(i)) {
                    collision.addIntersection(i);
                }
            }
        }
    }

    /**
     * Ray-casts along a given line and returns all intersections found.
     *
     * @param ray    A line, ray, or line segment. Treats {@code ray.p1} as the origin of the ray-cast.
     * @param filter A condition defining which intersections to return.
     * @return A {@link Collision} object containing all {@link Intersection}s found.
     */
    public static Collision rayCast(Line ray, Predicate<Intersection> filter) {
        ArrayList<Collider> colliders = ColliderGrid.all();

        Collision collision = new Collision();
        for(Collider collider : colliders) {
            collider.rayCheck(null, collision, ray, false, filter);
        }

        return collision;
    }

    /**
     * Sweeps for collisions with other {@link Collider} objects. Checks against objects near {@code position}.
     *
     * @param position       The starting position for the sweep.
     * @param delta_position Change in position to sweep across.
     * @param filter         A condition defining which intersections to return.
     * @return A {@link Collision} containing all of the intersections encountered during the sweep.
     */
    public Collision sweep(Vector2 position, Vector2 delta_position, Predicate<Intersection> filter) {
        Collision c = new Collision();
        if(!enabled) {
            return c;
        }

        setPosition(position);
        Vector2 reverse_delta_position = delta_position.multiply(-1);

        ArrayList<Collider> nearby = ColliderGrid.inNeighboringZones(this.position);

        for(Collider other : nearby) {
            if(other == this) {
                continue;
            }

            // Check this against other
            for(Vector2 vertex : this.getVertices()) {
                Line ray = new Line(vertex, vertex.sum(delta_position));
                other.rayCheck(this.object, c, ray, false, filter);
            }

            // Check other against this
            for(Vector2 vertex : other.getVertices()) {
                Line ray = new Line(vertex, vertex.sum(reverse_delta_position));
                this.rayCheck(other.object, c, ray, true, filter);
            }
        }
        return c;
    }

    /**
     * Sweeps for collisions with other {@link Collider} objects. Checks against objects near {@code position}.
     *
     * @param position       The starting position for the sweep.
     * @param delta_position Change in position to sweep across.
     * @return A {@link Collision} containing all of the intersections encountered during the sweep.
     */
    public Collision sweep(Vector2 position, Vector2 delta_position) {
        return sweep(position, delta_position, i -> true);
    }


    /* Events */

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

            for(Vector2 p : zone_check_points) {
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
        return net_cw_angle - Math.PI*2 > Misc.delta;
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
}
