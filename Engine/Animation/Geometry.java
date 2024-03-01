package Engine.Animation;
import java.util.*;

public class Geometry
{
	public float[] position;
	public float[] normal;
	public float[] coord;
	public int[] indices;
	public float[] weights;
	public float[] joints;
	public List<String> bone_names;
	public List<float[]> bindMatrices;
	public String mesh_id;
	public String skin_id;
	public String skeleton_id;
	public Joint rootJoint;
	
}
