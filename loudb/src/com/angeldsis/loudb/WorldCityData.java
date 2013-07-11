package com.angeldsis.loudb;

import com.angeldsis.louapi.world.AllianceMapping;
import com.angeldsis.louapi.world.CityMapping;
import com.angeldsis.louapi.world.PlayerMapping;

public class WorldCityData {
	public String name;
	public int playerid;
	public boolean castle;
	public int score;
	public WorldCityData(CityMapping city, AllianceMapping a, PlayerMapping p) {
		name = city.name;
		playerid = p.id;
		castle = city.Castle;
		score = city.Points;
	}
}
