package com.angeldsis.louapi.world;

import com.angeldsis.louapi.world.WorldParser.MapItem;

public class Moongate extends MapItem {
	/* output-1.js, $I.VGD.prototype.QGD
	 *    77456     this.iActivationStep = (d.$r = $I.CQ.QQ(b, (c + 1), d), e = d.c, d.$r);
	 *    77457     this.eMoongateState = (f & 15);
	 */
	public int state;
	public int activationStep;

}
