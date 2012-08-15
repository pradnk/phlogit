package com.intuit.project.phlogit.socialnetwork;

import java.util.HashMap;

import com.intuit.project.phlogit.data.vo.Profile;

public abstract class SocialNetwork {
	public static final int SOCIAL_NETWORK_FACEBOOK = 0;
	public static final int SOCIAL_NETWORK_TWITTER = 1;
	public static final int SOCIAL_NETWORK_LINKEDIN = 2;
	public static final int SOCIAL_NETWORK_INTUIT = 4;
	public static final int SOCIAL_NETWORK_YAMMER = 3;
	
	public static HashMap<Integer, SocialNetwork> providerClasses =
		    new HashMap<Integer, SocialNetwork>();
	public static HashMap<String, String[]> providerCredentials =
			new HashMap<String, String[]>();
	
	public abstract boolean isAuthenticated();
	
	public abstract void authenticate();
	
	public abstract void authenticate(boolean fetchProfile);
	
	public abstract void postStatus(String status);
	
	public abstract Profile fetchProfile();
	
	public abstract void addFriend(String id);
	
	public abstract String getName();
	
	public abstract int getId();
	
	public abstract void setTokens(Object... params);
	
	public static void addProvider(SocialNetwork network, String[] credentials) {
	    if (network != null && credentials != null && credentials.length == 2) {
	      providerClasses.put(network.getId(), network);
	      providerCredentials.put(network.getName(), credentials);
	    }
	}
	
	public static SocialNetwork getProvider(int id) {
		SocialNetwork socialNetwork = null;
		Object obj = providerClasses.get(id);
		if(obj != null) {
			if(obj instanceof TwitterSocialNetwork) {
				socialNetwork = (TwitterSocialNetwork)obj;
			} else if(obj instanceof FacebookSocialNetwork) {
				socialNetwork = (FacebookSocialNetwork)obj;
			}
		}
		return socialNetwork;
	}
}
