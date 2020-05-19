package engine;

import engine.objects.PhysicsObject;
import engine.objects.Collider;
import engine.util.Vector2;
import engine.TileLayer.TileSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Loads JSON and XML files generated by Tiled.
 *
 * @author Galen Savidge
 * @version 5/13/2020
 */
public class LevelParser {

    /* JSON parsing */

    /**
     * An interface for creating the lambda functions used to spawn objects.
     * <p>
     * Args list: "type" : (String) The object's type name. Should be identical to the {@link PhysicsObject}'s {@code
     * type} attribute. "position" : ({@link Vector2}) The starting position of the object in the game world. "vertices"
     * : ({@link Vector2}{@code []}) A list of polygon collider vertices. "solid", "visible", "persistent" : (bool) See
     * the corresponding attributes of {@link PhysicsObject}. Returns the {@link PhysicsObject} created, if applicable.
     */
    public interface TypeMap {
        PhysicsObject spawn(HashMap<String, Object> args);
    }


    /**
     * Loads the tiles, objects, and images from a JSON map file exported from Tiled.
     *
     * @param directory    The path to the folder containing the file.
     * @param file_name    The name of the JSON file.
     * @param constructors A map of object names to object constructor lambda functions. To call the correct constructor
     *                     the {@link PhysicsObject}'s {@code type} should match the type set in Tiled. Type names
     *                     should be lower case.
     */
    public static void loadFromJson(String directory, String file_name, HashMap<String, TypeMap> constructors) {
        JSONParser parser = new JSONParser();
        String file_text = getFileText(directory + file_name);

        try {
            JSONObject main = (JSONObject)parser.parse(file_text); // Top level node

            // Set up world
            World.grid_size = (int)(long)main.get("tilewidth")*World.grid_scaling_factor;
            World.width = (int)(long)main.get("width")*World.grid_size;
            World.height = (int)(long)main.get("height")*World.grid_size;

            // Need to do this between when the grid size is determined and when objects are instantiated
            Collider.initColliders();

            // List of all PhysicsObjects instantiated
            ArrayList<PhysicsObject> instances = new ArrayList<>();

            // Parse tile sets
            ArrayList<TileSet> tile_sets = parseTilesets(main, directory);

            // Parse layers
            JSONArray layers = (JSONArray)main.get("layers");
            for(Object l : layers) {
                JSONObject layer = (JSONObject)l;

                String layer_type = (String)layer.get("type");

                // Get layer offset
                Object x_object = layer.get("offsetx");
                double x = 0;
                if(x_object != null) {
                    x = Double.parseDouble(x_object.toString())*World.grid_scaling_factor;
                }
                Object y_object = layer.get("offsety");
                double y = 0;
                if(y_object != null) {
                    y = Double.parseDouble(y_object.toString())*World.grid_scaling_factor;
                }

                // Parse object group
                if(layer_type.equals("objectgroup")) {
                    JSONArray objects = (JSONArray)layer.get("objects");
                    instances.addAll(parseObjects(directory, x, y, objects, constructors));
                }

                // Parse tile/image layers
                if(layer_type.equals("tilelayer") || layer_type.equals("imagelayer")) {
                    parseImageLayer(directory, x, y, layer, tile_sets);
                }
            }

            // Call world loaded events
            for(PhysicsObject instance : instances) {
                instance.worldLoadedEvent();
            }
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
    }


    /* XML parsing functions */

    /**
     * Opens an XML document and returns a parsable {@link Document}.
     */
    public static Document XMLOpen(String file_name) {
        Document d = null;

        try {
            File f = new File(file_name);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            d = builder.parse(f);
            //d.getDocumentElement().normalize();
        }
        catch(ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    /**
     * Returns a list of all children of {@link Node} n that have the name {@code name}.
     */
    public static ArrayList<Node> XMLGetChildrenByName(Node n, String name) {
        ArrayList<Node> r = new ArrayList<>();
        NodeList n_children = n.getChildNodes();
        for(int i = 0;i < n_children.getLength();i++) {
            Node child = n_children.item(i);
            if(child.getNodeName().equals(name)) {
                r.add(child);
            }
        }
        return r;
    }


    /* Helper functions */

    /**
     * Returns the full text of {@code file_name} as a {@link String}. Returns {@code null} if the file could not be
     * opened.
     */
    private static String getFileText(String file_name) {
        File file = new File(file_name);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder file_text = new StringBuilder();
        while(scanner.hasNextLine()) {
            file_text.append(scanner.nextLine());
        }
        return file_text.toString();
    }

    /**
     * Creates {@link TileSet} objects from each tileset in a JSON file generated by Tiled.
     *
     * @param main      The top level JSON object for the file.
     * @param directory The path to the folder containing the JSON file.
     */
    private static ArrayList<TileSet> parseTilesets(JSONObject main, String directory) {
        ArrayList<TileSet> tile_sets = new ArrayList<>();

        JSONArray ts = (JSONArray)main.get("tilesets");
        for(Object t : ts) {
            String source_file = (String)((JSONObject)t).get("source");
            long first_gid = (long)((JSONObject)t).get("firstgid");
            tile_sets.add(new TileSet(directory, source_file, (int)first_gid));
        }

        return tile_sets;
    }

    /**
     * Parses object data from a {@link JSONArray} of object data. Instantiates objects using the lambda functions in
     * {@code constructors}.
     *
     * @see #loadFromJson
     */
    private static ArrayList<PhysicsObject> parseObjects(String directory, double xoffset, double yoffset,
                                                         JSONArray objects, HashMap<String, TypeMap> constructors) {
        ArrayList<PhysicsObject> instances = new ArrayList<>();
        if(objects == null) {
            return instances;
        }
        for(Object o : objects) {
            JSONObject object = (JSONObject)o;

            // Make list of args
            HashMap<String, Object> args = new HashMap<String, Object>(object);

            // Parse template
            String template_file = (String)object.get("template");
            if(template_file != null) {
                HashMap<String, Object> template_data = XMLParseTemplate(directory + template_file);
                args.putAll(template_data);
            }

            // Parse type
            Object type_obj = args.get("type");
            String type;
            if(type_obj != null) {
                type = ((String)type_obj).toLowerCase();
                args.put("type", type);
            }

            // Get position vector
            Object o_x = args.get("x");
            Object o_y = args.get("y");
            Object o_h = args.get("height");
            if(o_x != null && o_y != null) {
                double x = Double.parseDouble(o_x.toString())*World.grid_scaling_factor + xoffset;
                double y = Double.parseDouble(o_y.toString())*World.grid_scaling_factor + yoffset
                        - Double.parseDouble(o_h.toString())*World.grid_scaling_factor;
                args.put("position", new Vector2(x, y));
            }


            // Get vertices
            JSONArray o_vertices = (JSONArray)args.get("polygon");
            if(o_vertices != null) {
                Vector2[] vertices = new Vector2[o_vertices.size()];
                int i = 0;
                for(Object v : o_vertices) {
                    JSONObject vertex = (JSONObject)v;
                    double x = Double.parseDouble(vertex.get("x").toString())*World.grid_scaling_factor;
                    double y = Double.parseDouble(vertex.get("y").toString())*World.grid_scaling_factor;
                    vertices[i++] = new Vector2(x, y);
                }
                args.put("vertices", vertices);
            }

            // Parse properties
            JSONArray properties = (JSONArray)args.get("properties");
            args.putAll(parseProperties(properties));
            args.remove("properties");

            // Make instance
            String instance_type = (String)args.get("type");
            TypeMap constructor = constructors.get(instance_type);
            if(constructor != null) {
                instances.add(constructor.spawn(args));
            }
        }
        return instances;
    }

    /**
     * Parses tile and image layers from a {@link JSONObject} containing one layer from a Tiled map. Instantiates either
     * a new {@link TileLayer} or {@link ImageLayer} object depending on the contents of {@code layer}.
     *
     * @param directory The path to the folder containing the JSON file.
     * @param tile_sets The list of tilesets, likely returned by {@link #parseTilesets}.
     */
    private static void parseImageLayer(String directory, double xoffset, double yoffset, JSONObject layer,
                                        ArrayList<TileSet> tile_sets) {
        String layer_type = (String)layer.get("type");

        // Get layer custom properties
        JSONArray properties = (JSONArray)layer.get("properties");
        HashMap<String, Object> layer_properties = parseProperties(properties);
        Object parallax_object = layer_properties.get("parallax");
        double parallax = 1.0;
        if(parallax_object != null) {
            parallax = Double.parseDouble(parallax_object.toString());
        }
        Object tile_layer_object = layer_properties.get("layer");
        int tile_layer = 0;
        if(tile_layer_object != null) {
            tile_layer = (int)(long)tile_layer_object;
        }

        // Create tile layer object
        if(layer_type.equals("tilelayer")) {
            JSONArray tiles = (JSONArray)layer.get("data");
            if(tiles != null) {
                Object[] raw_tile_data = tiles.toArray();
                int[] tile_data = new int[raw_tile_data.length];
                for(int i = 0;i < tile_data.length;i++) {
                    tile_data[i] = (int)(long)raw_tile_data[i];
                }
                new TileLayer(xoffset, yoffset, tile_layer, parallax, tile_sets, tile_data);
            }
        }

        // Create image layer object
        if(layer_type.equals("imagelayer")) {
            Object tile_object = layer_properties.get("tile");
            boolean tile = false;
            if(tile_object != null) {
                tile = (boolean)tile_object;
            }
            Object scale_object = layer_properties.get("scale");
            int scale = 1;
            if(scale_object != null) {
                scale = (int)(long)scale_object;
            }
            String image_file_name = (String)layer.get("image");
            new ImageLayer(xoffset, yoffset, tile_layer, scale, parallax, tile, directory + image_file_name);
        }
    }

    /**
     * Creates a set of all of the properties in any "properties" node of a Tiled JSON file. Property names are
     * converted to lower case.
     *
     * @param properties The list of objects under the "properties" node.
     * @return The set of all properties at the node in the format {@code name:value}.
     */
    private static HashMap<String, Object> parseProperties(JSONArray properties) {
        HashMap<String, Object> data = new HashMap<>();
        if(properties != null) {
            for(Object p : properties) {
                JSONObject property = (JSONObject)p;
                String property_name = ((String)property.get("name")).toLowerCase();
                Object property_value = property.get("value");
                data.put(property_name, property_value);
            }
        }
        return data;
    }

    /**
     * Parses object attributes from an object template from Tiled. Template files are in XML and have the extension
     * .tx. Attributes parsed are type, collider vertices, and custom properties. See {@link TypeMap} for a list of
     * properties.
     *
     * @param file_name The name and path to the file.
     * @return A list of attributes in the format {@code name:value}.
     */
    private static HashMap<String, Object> XMLParseTemplate(String file_name) {
        HashMap<String, Object> data = new HashMap<>();
        Document template = XMLOpen(file_name);
        if(template == null) {
            return data;
        }

        Node xml_object = template.getElementsByTagName("object").item(0);
        Element object_element = (Element)xml_object;

        // Type
        String type = object_element.getAttribute("type");
        if(!type.equals("")) {
            data.put("type", type.toLowerCase());
        }

        // Height
        String height = object_element.getAttribute("height");
        if(!height.equals("")) {
            data.put("height", Double.parseDouble(height));
        }
        else {
            data.put("height", 0.0);
        }

        // Properties
        Node properties_node = object_element.getElementsByTagName("properties").item(0);
        if(properties_node != null) {
            NodeList properties = ((Element)properties_node).getElementsByTagName("property");
            for(int i = 0;i < properties.getLength();i++) {
                Element property = (Element)properties.item(i);
                String property_name = property.getAttribute("name");
                String property_type = property.getAttribute("type");
                Object property_value = property.getAttribute("value");
                switch(property_type) {
                    case "bool":
                        property_value = Boolean.parseBoolean((String)property_value);
                        break;
                    case "int":
                        property_value = Long.parseLong((String)property_value);
                        break;
                    case "float":
                        property_value = Double.parseDouble((String)property_value);
                        break;
                }
                data.put(property_name, property_value);
            }
        }

        // Vertices
        Element polygon = (Element)object_element.getElementsByTagName("polygon").item(0);
        if(polygon != null) {
            String polygon_data = polygon.getAttribute("points");
            String[] points = polygon_data.split(" ");
            Vector2[] vertices = new Vector2[points.length];
            int i = 0;
            for(String point : points) {
                String[] xy = point.split(",");
                vertices[i++] = new Vector2(Double.parseDouble(xy[0])*World.grid_scaling_factor,
                        Double.parseDouble(xy[1])*World.grid_scaling_factor);
            }
            data.put("vertices", vertices);
        }

        return data;
    }
}
