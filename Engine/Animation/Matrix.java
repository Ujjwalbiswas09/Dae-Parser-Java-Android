package Engine.Animation;

import com.badlogic.gdx.math.Matrix4;

public class Matrix {

    public static void transposeM(float[] arr2, int i0, float[] arr, int i1) {
        float[] ar = new Matrix4(arr).tra().getValues();
        for(int i =0;i < ar.length;i++){
            arr2[i]=ar[i];
        }
    }

    public static void invertM(float[] lc, int i0, float[] floats, int i1) {
        float[] ar = new Matrix4(floats).inv().getValues();
        for(int i =0;i < ar.length;i++){
            lc[i]=ar[i];
        }
    }

    public static void setIdentityM(float[] m, int i1) {
        float[] ar = new Matrix4().idt().getValues();
        for(int i =0;i < ar.length;i++){
            m[i]=ar[i];
        }
    }

    public static void multiplyMM(float[] globalMatrix, int i0, float[] parent, int i1, float[] localMatrix, int i2) {
        float[] ar = new Matrix4(parent).mul( new Matrix4(localMatrix)).getValues();
        for(int i =0;i < ar.length;i++){
            globalMatrix[i]=ar[i];
        }
    }
}
