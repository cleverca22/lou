package com.angeldsis.louapi.data;

import com.angeldsis.louapi.Player;

public class SubRequest {
	public enum Role { giver,receiver };
	// s is 1 for un-accepted
	// s is 2 for accepted subs
	public int state;
	public Player giver;
	public Player receiver;
	public Role role;
	public int id;

}
