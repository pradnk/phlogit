package com.intuit.project.phlogit.data.vo;

import java.io.Serializable;
import java.util.ArrayList;

public class FacebookCommentsLike implements Serializable {
	/**
	 * Generated SerialVersionUID
	 */
	private static final long serialVersionUID = -4906946536171743196L;
	public int likeNumber;
	public String likedBy;
	public ArrayList<String> comments = new ArrayList<String>();
	public ArrayList<String> from = new ArrayList<String>();
	
	@Override
	public String toString() {
		return "Like "+likeNumber+" Liked By: "+likedBy+"\n"+comments.toString();
	}
}
