package Engine.Animation;
import java.util.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;public class Animator
		{

				public Animation anime;
				public float delta;
				public HashMap<String,float[]> currentFrame = new HashMap<>();
				public int index=0;
				public int next=0;

				public void update(float del){
								delta += del;

								float current = anime.timeStamps[index];
								float nxt = anime.timeStamps[index+1];

								float dis1 = nxt - current;
								float dis2 = nxt - delta;

								float val = dis2 / dis1;

								if(delta >= current){
								index++;
								}
					
								if(!(index+1 < anime.timeStamps.length)){
								index =0;
								delta=0;
								}
								currentFrame.clear();
								for(int i=0;i < anime.parts.size();i++){
								AnimationPart part = anime.parts.get(i);
								float[] matrix1 = part.join_transforma.get(index);
								float[] matrix2 = part.join_transforma.get(index+1);
								currentFrame.put(part.name,calculate(matrix1,val,matrix2,1-val));
										}

						}

				public static float[] calculate(float[] a,float v1,float[] b,float v2){
								float[] mat = new float[a.length];
								for(int i=0;i<mat.length;i++){
								mat[i] = (b[i] * v1) + (a[i] * v2);
								}
								return mat;
						}
		}
