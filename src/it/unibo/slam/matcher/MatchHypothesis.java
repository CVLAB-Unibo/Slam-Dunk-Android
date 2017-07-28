package it.unibo.slam.matcher;

import java.util.Comparator;

/**
 * Match hypothesis struct.
 */
public class MatchHypothesis
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
     * Match ratio.
     */
    public float ratio;
    
    /**
     * Comparator based on the ratio value.
     */
    public static Comparator<MatchHypothesis> RATIO_COMPARATOR = new Comparator<MatchHypothesis>()
    {
		@Override
		public int compare(MatchHypothesis lhs, MatchHypothesis rhs)
		{
			return (lhs.ratio < rhs.ratio) ? -1 : ((lhs.ratio == rhs.ratio) ? 0 : 1);
		}
    };
}
