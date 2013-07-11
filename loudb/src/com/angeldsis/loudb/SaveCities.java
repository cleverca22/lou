package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class SaveCities extends Transaction {
	private ArrayList<CityToSave> cities;
	public SaveCities(ArrayList<CityToSave> cities) {
		this.cities = cities;
	}
	@Override void internalRun() throws SQLException {
		PreparedStatement findCellId = link.prepare("SELECT cellid FROM cell WHERE x = ? AND y = ?");
	}
}
