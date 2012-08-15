package com.intuit.project.phlogit.socialnetwork;

import static com.intuit.project.phlogit.socialnetwork.SocialNetwork.*;

public class SocialNetworkFactory {

	public static SocialNetwork getSocialNetwork(int id) {
		switch(id) {
		case SOCIAL_NETWORK_FACEBOOK:
			return new FacebookSocialNetwork(null);
		case SOCIAL_NETWORK_TWITTER:
			return new TwitterSocialNetwork(null);
		default:
			//Invalid case.
			return null;
		}
	}
	
}
