uniform mat4 uOrthoMatrix;
attribute vec2 aTexCoordinate;
attribute vec4 aPosition;
varying vec2 vTexCoordinate;

void main()
{
	gl_Position = uOrthoMatrix * aPosition;
	vTexCoordinate = aTexCoordinate;
}