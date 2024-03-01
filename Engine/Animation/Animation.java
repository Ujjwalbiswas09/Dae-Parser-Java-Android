package Engine.Animation;
import java.util.*;
import java.io.*;
import org.json.*;
public class Animation
{
	public List<AnimationPart> parts = new ArrayList<>();
	public float[] timeStamps = null;
	
	public void print(){
	for(AnimationPart l : parts){
	System.out.println(l.name);
	System.out.println(l.join_transforma.size());
	}
	}
	

}
