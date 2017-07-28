package it.unibo.slam.matcher;

/**
 * Match struct.
 */
public class FMatch
{
	/**
     * Query descriptor index.
     */
    public int queryIdx;
    
    /**
     * Train descriptor index.
     */
    public int trainIdx;
    
    /**
     * Train image index.
     */
    public int imgIdx;

    /**
     * Match score.
     */
    public float score;
}
