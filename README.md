# Dae-Parser-Java-Android
Dae Parser For Android Java

#Demo Code
```java

public class AndroidLauncher extends AndroidApplication implements ApplicationListener {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(this, config);


	}

	CameraInputController controller;
	PerspectiveCamera camera;
	public ModelBatch batch;
	ModelInstance instance;
	ShaderProgram program;
	Animator animator = new Animator();
	Geometry m;
	short[] arr;
	VertexBufferObject position;
	VertexBufferObject normal;
	VertexBufferObject texcoord;
	VertexBufferObject weight;
	VertexBufferObject joint;
	IndexBufferObject indices;

	@Override
	public void create() {
		ColladaLoader colla = new ColladaLoader();
		colla.load(Gdx.files.internal("Jump.dae").read(),4);

		m = colla.geometries.get(0);
		final float[] finalVerts = new float[m.position.length+m.coord.length+m.normal.length];

		animator.anime = colla.loadAnimation(Gdx.files.internal("Jump.dae").read());

		camera = new PerspectiveCamera();
		Camera cam = camera;
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 0.01f;
		cam.far = 1000f;
		cam.update();

		controller = new CameraInputController(camera);
		Gdx.input.setInputProcessor(controller);
		batch = new ModelBatch();
		Log.e("t","Compl"+m.position.length);

		program = new ShaderProgram(VERTEX_CODE,FRAGMENT_CODE);
		Log.e("x", program.getLog());
		int boneID = program.getAttributeLocation("joints");
		int weightId = program.getAttributeLocation("weights");
		int PositionID = program.getAttributeLocation("position");
		int CoordID = program.getAttributeLocation("coord");
		int NormalID = program.getAttributeLocation("normal");
		position = new VertexBufferObject(true,m.position.length,new VertexAttribute(PositionID,3,"position"));
		position.setVertices(m.position,0,m.position.length);

		normal = new VertexBufferObject(true,m.normal.length,new VertexAttribute(NormalID,3,"normal"));
		normal.setVertices(m.normal,0,m.normal.length);

		joint = new VertexBufferObject(true,m.joints.length,new VertexAttribute(boneID,4,"joints"));
		joint.setVertices(m.joints,0,m.joints.length);

		weight = new VertexBufferObject(true,m.weights.length,new VertexAttribute(weightId,4,"weights"));
		weight.setVertices(m.weights,0,m.weights.length);

		indices = new IndexBufferObject(true,m.indices.length);
		arr  = new short[ m.indices.length];
		for(int i =0;i<m.indices.length;i++)
    arr[i] =(short) m.indices[i];
		indices.setIndices(arr,0,arr.length);

	}


	public Array<ModelMaterial> materials = new Array<ModelMaterial>();
	public ModelMaterial getMaterial (final String name) {
		for (final ModelMaterial m : materials)
			if (m.id.equals(name)) return m;
		ModelMaterial mat = new ModelMaterial();
		mat.id = name;
		mat.diffuse = new Color(Color.WHITE);
		materials.add(mat);
		return mat;
	}
	@Override
	public void resize(int width, int height) {
		camera.viewportHeight=height;
		camera.viewportWidth=width;

	}

	@Override
	public void render() {
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT|
				GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glClearColor(0.3f,0.3f,0.3f,1);
		controller.update();
		camera.update();
		program.begin();

		animator.update(Gdx.graphics.getDeltaTime());
		m.rootJoint.applyAnime(animator.currentFrame);
		m.rootJoint.calculateAnimationMatrix(null);
		m.rootJoint.uniformMatrices(program,"bone");

		program.setUniformMatrix("project",camera.projection);
		program.setUniformMatrix("view",camera.view);
		program.setUniformMatrix("model",new Matrix4());



		position.bind(program);
		normal.bind(program);
		joint.bind(program);
		weight.bind(program);

		indices.bind();
		Gdx.gl.glDrawElements(GL20.GL_TRIANGLES, arr.length, GL20.GL_UNSIGNED_SHORT, 0);

		indices.unbind();

		position.unbind(program);
		normal.unbind(program);
		joint.unbind(program);
		weight.unbind(program);
		program.end();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {

	}

	public static final String VERTEX_CODE =
			"attribute vec4 position;\n"+
					"attribute vec4 weights;\n"+
					"attribute vec4 joints;\n"+
					"attribute vec2 coord;\n"+
					"attribute vec3 normal;\n"+
					"uniform mat4 view;\n"+
					"uniform mat4 project;\n"+
					"uniform mat4 bone[60];\n"+
					"uniform mat4 model;\n"+
					"varying vec4 v_color;\n"+
					"varying vec3 v_normal;\n"+
					"varying vec2 v_coord;\n"+
					"void main(){\n"+
					"vec4 total = vec4(0.0);\n"+
					"vec4 pos = position;\n"+
					"vec4 total_normal = vec4(0.0);\n"+
					"vec4 mycolor = vec4(0.4);\n"+
					"for(int i=0;i < 4;i++){\n"+
					"int id = int(joints[i]);\n"+
					"if(id != -1){\n"+
					"	mat4 tmp = bone[id];\n"+
					"	float f = weights[i];\n"+
					"	vec4 po =  tmp * (f * pos);\n"+
					"	vec4 no = tmp * (f * vec4(normal,0.0));\n"+
					"	total += po;\n"+
					"	total_normal += no;\n"+
					"	}"+
					"}\n"+
					"v_normal = total_normal.xyz;\n"+
					"v_color = mycolor;\n"+
					"v_coord = vec2(coord.x,1.0-coord.y);\n"+
					"gl_Position = project * view * model * total;\n"+
					"}\n";
	public static final String FRAGMENT_CODE =
			"#ifdef GL_ES \n"+
					"#define LOWP lowp\n"+
					"#define MED mediump\n"+
					"#define HIGH highp\n"+
					"precision mediump float;\n"+
					"#endif\n"+
					"varying vec3 v_normal;\n"+
					"varying vec2 v_coord;\n"+
					"varying vec4 v_color;\n"+
					"uniform sampler2D diffuse;\n"+
					"uniform vec3 sun;\n"+
					"uniform vec4 color;\n"+
					"uniform int hasTexture;\n"+
					"void main(){\n"+
					"vec4 fcolor = color;\n"+
					"if(hasTexture==1){\n fcolor *= texture2D(diffuse,v_coord);\n}\n"+
					"float amb = clamp(dot(-v_normal,sun),0.25,1.0);\n"+
					"gl_FragColor = vec4(amb,amb,amb,1.0)* fcolor;\n"+
					"}";


}

```java
