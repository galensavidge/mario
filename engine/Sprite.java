package engine;

import java.awt.*;
import java.util.ArrayList;

public class Sprite {
    ArrayList<Image> frames = new ArrayList<>();
    private int current_frame;
    private int frame_time = 1;
    private int frame_counter;

    public Sprite(String[] filenames) {
        for(String filename : filenames) {
            frames.add(GameGraphics.getImage(filename));
        }
        current_frame = 0;
        frame_counter = 1;
    }

    public void setFrameTime(int frames) {
        if(frames >= 1) {
            frame_time = frames;
        }
    }

    public Image getCurrentFrame() {
        frame_counter++;
        if(frame_counter > frame_time) {
            frame_counter = 1;
            current_frame++;

            if(current_frame == frames.size()) {
                current_frame = 0;
            }
        }

        return frames.get(current_frame);
    }
}
