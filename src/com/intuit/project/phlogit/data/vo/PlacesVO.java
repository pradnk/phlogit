package com.intuit.project.phlogit.data.vo;

import java.util.ArrayList;

public class PlacesVO {
	public String lat;
	public String lng;
	public String icon;
	public String name;
	public String id;
	public String vicinity;
	public String rating;
	public ArrayList<String> types = new ArrayList<String>();
	
	@Override
	public String toString() {
		return lat+" "+lng+" "+icon+" "+name+" "+id+" "+vicinity+" "+rating;
	}
}
