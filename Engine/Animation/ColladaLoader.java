package Engine.Animation;
import java.util.*;
import java.io.*;

public class ColladaLoader
{
	public List<Geometry> geometries = new ArrayList<>();
	private int max;
	public void load(InputStream stream,int max){
		this.max = max;
	XmlNode collada = new XmlNode().load(stream).getChild("COLLADA");
	List<XmlNode> geometries_node = collada.getChild("library_geometries").getChilds("geometry");
	List<XmlNode> library_skin = collada.getChild("library_controllers").getChilds("controller");
	for(XmlNode geo : geometries_node){
		parseGeomerty(geo,library_skin,collada);
	}
	}
	
	private void parseGeomerty(XmlNode geo,List<XmlNode> skins,XmlNode collada){
	Geometry tmp = new Geometry();
	XmlNode mesh = geo.getChild("mesh");
	tmp.mesh_id = geo.getAttribute("id");
	XmlNode controller = getValidSkin(skins,tmp.mesh_id);
	XmlNode skin = controller.getChild("skin");
	tmp.skin_id = controller.getAttribute("id");
	XmlNode polylist = mesh.getChild("polylist");
	String position_id = mesh.getChild("vertices").getChildWithAttributeValue("input","semantic","POSITION")
	.getAttribute("source").substring(1);
	    String normal_id = polylist.getChildWithAttributeValue("input","semantic","NORMAL").getAttribute("source").substring(1);
		String coord_id = polylist.getChildWithAttributeValue("input","semantic","TEXCOORD").getAttribute("source").substring(1);
		int normal_offset = Integer.parseInt(polylist.getChildWithAttributeValue("input","semantic","NORMAL").getAttribute("offset"));
		int coord_offset = Integer.parseInt(polylist.getChildWithAttributeValue("input","semantic","TEXCOORD").getAttribute("offset"));
		String[] raw_vertices = mesh.getChildWithAttributeValue("source","id",position_id).getChild("float_array").getData().trim().split("\\s|\n");
		tmp.position = new float[raw_vertices.length];
		for(int i =0;i < raw_vertices.length;i++){
		tmp.position[i]= Float.parseFloat( raw_vertices[i]);
		}
		
		//you convert string[] to float[] for lower memory usage
		float[] raw_normal = toFloat( mesh.getChildWithAttributeValue("source","id",normal_id)
		.getChild("float_array").getData().trim().split("\\s|\n"));
		
		
		float[] raw_texcoord = toFloat( mesh.getChildWithAttributeValue("source","id",coord_id)
		.getChild("float_array").getData().trim().split("\\s|\n"));
		tmp.normal = new float[tmp.position.length];
		tmp.coord = new float[(tmp.position.length/3)*2];
		int max_offset = polylist.getChilds("input").size();
		int[] vcount = toInt( polylist.getChild("vcount").getData().trim().split("\\s|\n"));
		int[] raw_indices = toInt( polylist.getChild("p").getData().trim().split("\\s|\n"));
		int indice_position=0;
		int raw_indice_position=0;
		tmp.indices = new int[vcount.length * 3];
		for(int str :  vcount){
		for(int i=0;i<str;i++){
		 int value = raw_indices[raw_indice_position];
			int normal_index = 3 * raw_indices[raw_indice_position+normal_offset];
			int texcoord_index = 2 * raw_indices[raw_indice_position+coord_offset];
			int actutal_normal_position = value * 3;
			int actutal_tex_position = value * 2;

			tmp.normal[actutal_normal_position] = raw_normal[normal_index];
			tmp.normal[actutal_normal_position+1] = raw_normal[normal_index+1];
			tmp.normal[actutal_normal_position+2] = raw_normal[normal_index+2];

			tmp.coord[actutal_tex_position] = raw_texcoord[texcoord_index];
			tmp.coord[actutal_tex_position+1] = raw_texcoord[texcoord_index+1];

			tmp.indices[indice_position++] = value;
			raw_indice_position += max_offset;
			
		}
		}
		raw_vertices = null;
		raw_normal = null;
		raw_texcoord =null;
		raw_indices = null;
		vcount = null;
		
		String joints_id = skin.getChild("joints")
		.getChildWithAttributeValue("input","semantic","JOINT").getAttribute("source").substring(1);
		
		String bind_matice_id = skin.getChild("joints")
		.getChildWithAttributeValue("input","semantic","INV_BIND_MATRIX").getAttribute("source").substring(1);
		
		float[] raw_bind_matrices = toFloat( skin.getChildWithAttributeValue("source","id",bind_matice_id).getChild("float_array")
		.getData().trim().split("\\s|\n"));
		
		String[] raw_joint_names = skin.getChildWithAttributeValue("source","id",joints_id).getChild("Name_array")
		.getData().trim().split("\\s|\n");
		
		tmp.bone_names = Arrays.asList(raw_joint_names);
		tmp.bindMatrices = new ArrayList<>();
		int matrice_count  = raw_bind_matrices.length / 16;
		for(int i = 0; i < matrice_count;i++){
			float[] arr = new float[16];
			for(int p=0;p < 16;p++){
				int index = (i*16)+p;
				arr[p] = raw_bind_matrices[index];
			}
			float[] arr2 = new float[16];
			Matrix.transposeM(arr2,0,arr,0);
			tmp.bindMatrices.add(arr2);
		}
		XmlNode vertex_weight = skin.getChild("vertex_weights");
		String weight_id = vertex_weight.getChildWithAttributeValue("input","semantic","WEIGHT")
		.getAttribute("source").substring(1);
		
		float[] raw_weights = toFloat( skin.getChildWithAttributeValue("source","id",weight_id).
		getData().trim().split("\\s|\n"));
		vcount = toInt(vertex_weight.getChild("vcount").getData().trim().split("\\s|\n"));
		int[] v = toInt( vertex_weight.getChild("v").getData().trim().split("\\s|\n"));
		List<float[]> tmp_weights = new ArrayList<>();
		int raw_position =0;
		int weight_position =0;
		tmp.weights = new float[ (tmp.position.length / 3) * max];
		tmp.joints = new float[tmp.weights.length];
		for(int str : vcount){
		int count = str;
		tmp_weights.clear();
		for(int i=0;i < count;i++){
		float[] tmp_arr = new float[2];
		tmp_arr[0] = (v[raw_position++]);
		tmp_arr[1] = raw_weights[(v[raw_position++])];
		tmp_weights.add(tmp_arr);
		}
		normalizeWeights(tmp_weights);
		for(float[] i : tmp_weights){
		tmp.weights[weight_position] = i[1];
		tmp.joints[weight_position] = i[0];
		weight_position++;
		}
		}
		tmp.rootJoint = getJoint(getRootNode(collada.getChild("library_visual_scenes").
		getChild("visual_scene"),tmp.skin_id),tmp);
		geometries.add(tmp);
	}
	
	private XmlNode getValidSkin(List<XmlNode> skin,String source){
	for(XmlNode xml : skin){
		if(xml.getChild("skin").getAttribute("source").substring(1).equals(source)){
		return xml;
		}
	}
	return null;
	}
	
	private void normalizeWeights(List<float[]> ids){
		if(ids.size() < max){
			while(ids.size() < max){
				ids.add(new float[]{0f,0f});
			}
		}else if(ids.size() > max){
			Collections.sort(ids, new Comparator<float[]>(){
					@Override
					public int compare(float[] p1, float[]p2){
						return Float.compare(p2[1],p1[1]);
					}});
			while(ids.size() > max){
				ids.remove(max);
			}
			float tot = 0;
			for(float[] g : ids){
				tot += g[1];
			}

			for(int i=0;i < max;i++){
				float p = ids.get(i)[1] / tot;
				ids.get(i)[1] = p;
			}
			
			}
	}
	
	public XmlNode getRootNode(XmlNode scenceNode, String skinId){
		List<XmlNode> nodes = scenceNode.getChilds("node");
		for(XmlNode node : nodes){
			if(node.getChild("instance_controller") != null){
				XmlNode tmp = node.getChild("instance_controller");
				if(tmp.getAttribute("url").substring(1).equals(skinId)){
				String name= tmp.getChild("skeleton").getData().substring(1);
					XmlNode root = scenceNode.getChildWithAttributeValue("node","id",name);
					if(root == null){
					return scenceNode.getChildWithAttributeValue("node","id","Armature") .getChildWithAttributeValue("node","id",name);
					}else{
					return root;
					}
				}
			}
		}
	
		return null;
	}
	
	
	public Joint getJoint(XmlNode node,Geometry geo){
		Joint join = new Joint();
		String name;
		name = node.getAttribute("id");
		join.index= geo.bone_names.indexOf(name);
		join.name = name;
		if(join.index != -1){
			join.inverseMatrix = geo.bindMatrices.get(join.index);
			float[] lc = new float[16];
			Matrix.invertM(lc,0,geo.bindMatrices.get(join.index),0);
			join.localMatrix =lc;
		}
		if(node.getChild("matrix")!=null){
			String[] val = node.getChild("matrix").getData().trim().split("\\s");
			float[] mat = new float[16];
			join.localMatrix = new float[16];
			for(int i = 0;i < 16;i++){
				mat[i] = Float.parseFloat(val[i]);
			}
			join.localMatrix = new float[16];
			Matrix.transposeM(join.localMatrix,0, mat,0);
			join.animatedMatrix =  new float[16];
			Matrix.transposeM(join.animatedMatrix,0,mat,0);
		}
		for(XmlNode noe : node.getChilds("node")){
			join.childs.add(getJoint(noe,geo));
		}
		return join;
	}
	
	
	
	private Animation parseAnimation(XmlNode node){
	Animation animation = new Animation();
		List<XmlNode> chi = node.getChilds("animation");
		for(XmlNode per : chi){
			AnimationPart part = new AnimationPart();
			String joint_name = per.getChild("channel").getAttribute("target").split("/")[0];
			String output_id = per.getChild("sampler").getChildWithAttributeValue("input","semantic","OUTPUT").getAttribute("source").substring(1);
			String input_id = per.getChild("sampler").getChildWithAttributeValue("input","semantic","INPUT").getAttribute("source").substring(1);
			if(animation.timeStamps == null){
				String[] raw_input =  per.getChildWithAttributeValue("source","id",input_id).getChild("float_array").getData().trim().split("\\s");
				animation.timeStamps = new float[raw_input.length];
				for(int i=0;i<raw_input.length;i++){
					animation.timeStamps[i] = Float.parseFloat(raw_input[i]);
				}
			}
			float[] raw_mats = toFloat( per.getChildWithAttributeValue("source","id",output_id).getChild("float_array").getData().trim().split("\\s|\n"));
			part.name = joint_name;
			int size = raw_mats.length/16;
			for(int i = 0; i < size;i++){
				float[] arr = new float[16];
				for(int p=0;p < 16;p++){
					int index = (i*16)+p;
					arr[p] = raw_mats[index];
				}
				float[] arr2 = new float[16];
				Matrix.transposeM(arr2,0,arr,0);
				part.join_transforma.add(arr2);
			}
			animation.parts.add(part);
		}
		return animation;
	}
	
	public Animation loadAnimation(InputStream inp){
		XmlNode collada = new XmlNode().load(inp).getChild("COLLADA");
		return parseAnimation(collada.getChild("library_animations"));
	}
	
	private static float[] toFloat(String[] data){
		float[] arr = new float[data.length];
		for(int i=0;i < data.length;i++){
		arr[i] = Float.parseFloat(data[i]);
		}
		return arr;
	}
	
	private static int[] toInt(String[] data){
		int[] arr = new int[data.length];
		for(int i=0;i < data.length;i++){
			arr[i] = Integer.parseInt(data[i]);
		}
		return arr;
	}
}
