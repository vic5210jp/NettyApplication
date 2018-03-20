/**
 * @Title: RefreshAdjustFactorService.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service;

/**
 * @description 刷新复权因子相关服务
 *
 * @author 余俊斌
 * @date 2015年7月30日 下午7:14:21
 * @version v1.0
 */

public interface RefreshAdjustFactorService {

	/**
	 * 刷新个股复权因子
	 * @param date
	 */
	void refreshStkAF(String date);
	
	/**
	 * 刷新前复权的K线（周、月、年）</br>
	 * <b><i>调用此方法刷新之前，需要保证复权因子已经更新到最新</i></b>
	 * @param date
	 */
	void refreshForwardAdjust(String date);
}
