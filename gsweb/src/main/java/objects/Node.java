package objects;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Martin on 21.11.2015.
 */
public class Node {
    public UUID id;
    public String name;
    public byte area[][]=new byte[16][16];
    public UUID video_id;
    public ArrayList<Result> results=new ArrayList<Result>();
}
