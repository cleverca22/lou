package com.angeldsis.louapi.world;

import com.angeldsis.louapi.world.WorldParser.MapItem;

public class Shrine extends MapItem {
	public static final int inactive = 0;
	public static final int compassion = 1;
	public static final int honesty = 2;
	public static final int honor = 3;
	public static final int humility = 4;
	public static final int justice = 5;
	public static final int sacrifice = 6;
	public static final int spirituality = 7;
	public static final int valor = 8;
	public static final String[] types = {"Inactive","Compassion","Honesty","Honor","Humility","Justice","Sacrifice","Spirituality","Valor"};
	public int type;
}
