uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
uniform vec4 uColor;
varying vec4 vColor;

void main()
{
	gl_PointSize = 2.0;
	gl_Position = uMVPMatrix * aPosition;
	vColor = uColor;
}