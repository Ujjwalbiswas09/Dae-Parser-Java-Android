package Engine.Animation;




import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import java.util.*;
public class Joint
{
	public float[] localMatrix;
	public float[] inverseMatrix;
	public float[] animatedMatrix;
	public float[] globalMatrix = new float[16];
	public float[] finalMat = new float[16];
	public String name="null";
	public int index;
	public List<Joint> childs = new ArrayList<>();
	
	
	public void printInfo(){
	System.out.println("name: "+name);
	System.out.println("index: "+index);
	System.out.println("local matrix: "+Arrays.toString(localMatrix));
	System.out.println("inverse matrix : "+Arrays.toString(inverseMatrix));
	if(localMatrix != null){
		float[] localInverse = new float[16];
		Matrix.invertM(localInverse,0,localMatrix,0);
		System.out.println("inverse local: "+Arrays.toString(localInverse));
	}
	System.out.println();
	for(Joint t : childs){
	t.printInfo();
	}
	}
	
	
	public void applyAnime(HashMap<String,float[]> frame){
		if(frame.containsKey(name)){
		animatedMatrix = frame.get(name);
		}
		for(int i=0;i < childs.size();i++){
		childs.get(i) .applyAnime(frame);
		}
	}
	
	public void calculateMatrix(float[] parent){
	if(localMatrix == null){
	float[] m = new float[16];
	Matrix.setIdentityM(m,0);
	localMatrix = m;
	}
	if(parent == null){
	float[] m = new float[16];
	Matrix.setIdentityM(m,0);
	set(globalMatrix,localMatrix);
	}else{
	Matrix.multiplyMM(globalMatrix,0,parent,0,localMatrix,0);
	}
	if(index != -1){
	Matrix.multiplyMM(finalMat,0,globalMatrix,0,inverseMatrix,0);
	}
	for(Joint child : childs){
	child.calculateMatrix(globalMatrix);
	}
	}
	
	public void calculateAnimationMatrix(float[] parent){
		if(localMatrix == null){
		float[] m = new float[16];
		Matrix.setIdentityM(m,0);
		localMatrix = m;
		}
		if(parent == null){
		if(animatedMatrix == null){
		animatedMatrix = localMatrix.clone();
		}
		float[] m = new float[16];
		Matrix.setIdentityM(m,0);
		set(globalMatrix,animatedMatrix);
		}else{
		if(animatedMatrix == null){
		animatedMatrix=localMatrix.clone();
		}
		Matrix.multiplyMM(globalMatrix,0,parent,0,animatedMatrix,0);
		}
		
		if(index != -1){
		Matrix.multiplyMM(finalMat,0,globalMatrix,0,inverseMatrix,0);
		}
		for(int i=0;i < childs.size();i++){
		childs.get(i).calculateAnimationMatrix(globalMatrix);
		}
	}
	
	public void uniformMatrices(ShaderProgram prog, String name){
	if(index != -1){
	prog.setUniformMatrix4fv(name+"["+index+"]",finalMat,0,16);
	}
	for(int i=0;i < childs.size();i++){
	childs.get(i).uniformMatrices(prog,name);
	}
	
	}
	
	private void set(float[] a,float[] b){
		System.arraycopy(b, 0, a, 0, a.length);
	}

	
}
