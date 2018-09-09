/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms;

public interface IBlockInfo {
	int getX();
	int getY();
	int getZ();
	/**
	 * In 1.13, this is a combined block + state information based on a registry 
	 * @return
	 */
	int getTypeId();
}
