uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec4 aColor;
varying vec4 vColor;

void main()
{
	gl_PointSize = 2.0;
	gl_Position = uMVPMatrix * aPosition;
	vColor = aColor;
}