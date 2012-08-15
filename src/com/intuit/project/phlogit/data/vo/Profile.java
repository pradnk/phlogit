package com.intuit.project.phlogit.data.vo;

import java.io.Serializable;

public class Profile implements Serializable {
	/**
	 * Generated SerialVersionUID
	 */
	private static final long serialVersionUID = -4906946536171743196L;
	public String name;
	public String email;
	public String dob;
	public String bio;
	public byte[] image;
	public String source;
	//Optinally set for some like Twitter.
	public Long id;
	public String gender;
	
	@Override
	public String toString() {
		return name+" "+email+" "+dob+"  "+bio;
	}
}
