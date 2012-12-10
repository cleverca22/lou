package com.angeldsis.LOU;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CityResField extends LouVisData {
	String TAG = "CityResField";
	public int c,visId;
	// subid/typeid data
	// most images are 150x128
	// 0/0 image 221 townlayer/stone_m_1x1_01.png
	// 0/1 image 222 townlayer/stone_t_1x1_01.png
	// 0/2 image 223 townlayer/stone_b_1x1_01.png
	// 0/3 image 224 townlayer/stone_l_1x1_01.png
	// 0/4 image 89  townlayer/stone_destroy_resource.png
	
	// 1/0 image 225 townlayer/forrest_mid_01.png
	// 1/1 image 226 townlayer/forrest_big_01.png
	// 1/2 image 227 townlayer/forrest_big_02.png
	
	// 2/0 image 86  townlayer/iron_m_1x1_01.png
	// 2/1 image 228 townlayer/iron_r_1x1_02.png
	// 2/2 image 229 townlayer/iron_r_1x1_03.png
	
	// 3/0 effect 96 128x90 image 230 townlayer/animations/anim_lake_l_1x1_01/animseq_lake_l_1x1_01.png
	// 3/1 effect 97 128x90 image 231 townlayer/animations/anim_lake_m_1x1_01/animseq_lake_m_1x1_01.png
	// 3/2 effect 98 128x90 image 232 townlayer/animations/anim_lake_s_1x1_01/animseq_lake_s_1x1_01.png
	
	// 4/0 image 200 townlayer/stone_magic_resource.png
	// 5/0 image 202 townlayer/wood_magic_resource.png
	// 6/0 image 198 townlayer/iron_magic_resource.png
	// 7/0 effect 99 169x148 image 204 townlayer/lake_magic_resource.png
	public CityResField(JSONObject base) throws JSONException {
		super(base);
		subid = base.getInt("r");
		c = base.getInt("c");
		visId = base.getInt("i");
	}
}
