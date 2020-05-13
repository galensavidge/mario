package engine;

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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class LevelParser {

    public interface TypeMap {
        void spawn(Dictionary<String,Object> args);
    }

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

    /* XML parsing functions */

    public static Document parseXML(String file_name) {
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

    public static ArrayList<Node> getChildrenByName(Node n, String name) {
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

    public static void loadFromJson(String folder_path, String file_name, Dictionary<String, TypeMap> constructors) {
        JSONParser parser = new JSONParser();
        String file_text = getFileText(folder_path+file_name);

        try {
            JSONObject main = (JSONObject)parser.parse(file_text);

            long width = (long)main.get("width");
            long height = (long)main.get("height");
            long grid_size = (long)main.get("tilewidth")*World.grid_scaling_factor;
            World.grid_size = (int)grid_size;
            World.width = (int)width*World.grid_size;
            World.height = (int)height*World.grid_size;

            Collider.initColliders(2*World.grid_size); // Again, change this!

            System.out.println("Width: "+width+", Height: "+height+", Grid-size: "+grid_size);

            // Parse tile sets
            ArrayList<TileSet> tile_sets = new ArrayList<>();

            JSONArray ts = (JSONArray) main.get("tilesets");
            for(Object t : ts) {
                String source_file = (String)((JSONObject) t).get("source");
                long first_gid = (long)((JSONObject) t).get("firstgid");
                tile_sets.add(new TileSet(folder_path, source_file, (int)first_gid));
            }

            // Parse layers
            JSONArray layers = (JSONArray) main.get("layers");
            for(Object l : layers) {
                JSONObject layer = (JSONObject) l;

                // Parse objects
                JSONArray objects = (JSONArray) layer.get("objects");
                if(objects != null) {
                    for (Object o : objects) {
                        JSONObject object = (JSONObject) o;
                        String type = "";

                        // Make list of args
                        Dictionary<String, Object> args = new Hashtable<>();

                        // Put position vector in the list of args
                        Object o_x = object.get("x");
                        Object o_y = object.get("y");
                        if(o_x != null && o_y != null) {
                            long x = (long) object.get("x")*World.grid_scaling_factor;
                            long y = (long) object.get("y")*World.grid_scaling_factor;
                            args.put("position", new Vector2((double) x, (double) y));
                        }

                        // Parse template
                        String template_file = (String) object.get("template");
                        if(template_file != null) {
                            Document xml_template = parseXML(folder_path+template_file);
                            Node xml_object = xml_template.getElementsByTagName("object").item(0);
                            Element object_element = (Element) xml_object;
                            // Type
                            String xml_type = object_element.getAttribute("type");
                            if(xml_type != null) {
                                type = xml_type.toLowerCase();
                            }

                            // Properties
                            NodeList properties = ((Element)object_element.getElementsByTagName("properties").item(0)).getElementsByTagName("property");
                            for(int i = 0;i < properties.getLength();i++) {
                                Element property = (Element) properties.item(i);
                                String property_name = property.getAttribute("name");
                                String property_type = property.getAttribute("type");
                                Object property_value = property.getAttribute("value");
                                if(property_type.equals("bool")) {
                                    property_value = Boolean.parseBoolean((String)property_value);
                                }
                                args.put(property_name, property_value);
                            }

                            // Vertices
                            Element polygon = (Element) object_element.getElementsByTagName("polygon").item(0);
                            if(polygon != null) {
                                String data = polygon.getAttribute("points");
                                String[] points = data.split(" ");
                                Vector2[] vertices = new Vector2[points.length];
                                int i = 0;
                                for (String point : points) {
                                    String[] xy = point.split(",");
                                    vertices[i++] = new Vector2(Double.parseDouble(xy[0])*World.grid_scaling_factor,
                                                                Double.parseDouble(xy[1])*World.grid_scaling_factor);
                                }
                                args.put("vertices", vertices);
                            }
                        }

                        // Parse type
                        Object type_obj = object.get("type");
                        if(type_obj != null) {
                            type = ((String) type_obj).toLowerCase();
                        }

                        // Get vertices
                        JSONArray o_vertices = (JSONArray) object.get("polygon");
                        if(o_vertices != null) {
                            Vector2[] vertices = new Vector2[o_vertices.size()];
                            int i = 0;
                            for(Object v : o_vertices) {
                                JSONObject vertex = (JSONObject) v;
                                long x = (long) vertex.get("x")*World.grid_scaling_factor;
                                long y = (long) vertex.get("y")*World.grid_scaling_factor;
                                vertices[i++] = new Vector2((double) x, (double) y);
                            }
                            args.put("vertices", vertices);
                        }

                        // Parse properties
                        JSONArray properties = (JSONArray) object.get("properties");
                        if (properties != null) {
                            for (Object p : properties) {
                                JSONObject property = (JSONObject) p;
                                String property_name = (String) property.get("name");
                                Object property_value = property.get("value");
                                args.put(property_name, property_value);
                            }
                        }

                        // Make instance
                        TypeMap constructor = constructors.get(type);
                        if(constructor != null) {
                            constructors.get(type).spawn(args);
                        }
                    }
                }

                // Parse tiles
                JSONArray tiles = (JSONArray) layer.get("data");

                if(tiles != null) {
                    Object[] raw_tile_data = tiles.toArray();
                    int[] tile_data = new int[raw_tile_data.length];
                    for(int i = 0;i < tile_data.length;i++) {
                        tile_data[i] = (int)((long)raw_tile_data[i]);
                    }
                    new TileLayer(0, 1, tile_sets, tile_data);
                }
            }

        }
        catch(ParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }

}
