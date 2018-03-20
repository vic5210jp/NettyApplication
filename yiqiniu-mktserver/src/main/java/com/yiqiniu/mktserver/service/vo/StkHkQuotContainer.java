/**
 * @Title: StkAssetContainer.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.vo;

import com.yiqiniu.mktinfo.persist.po.StkDayHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHk;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkMonthHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkWeekHk;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkWeekHkFwd;
import com.yiqiniu.mktinfo.persist.po.StkYearHk;
import com.yiqiniu.mktinfo.persist.po.StkYearHkBkw;
import com.yiqiniu.mktinfo.persist.po.StkYearHkFwd;

/**
 * @description 上交所资产日、周、月、年含复权信息的存储结构
 *
 * @author 余俊斌
 * @date 2015年8月21日 下午3:30:58
 * @version v1.0
 */

public class StkHkQuotContainer {

	private String assetId;
	private StkDayHk stkDay;
	private StkWeekHk stkWeek;
	private StkWeekHkFwd stkWeekFwd;
	private StkWeekHkBkw stkWeekBkw;
	private StkMonthHk stkMonth;
	private StkMonthHkFwd stkMonthFwd;
	private StkMonthHkBkw stkMonthBkw;
	private StkYearHk stkYear;
	private StkYearHkFwd stkYearFwd;
	private StkYearHkBkw stkYearBkw;

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public StkDayHk getStkDay() {
		return stkDay;
	}

	public void setStkDay(StkDayHk stkDay) {
		this.stkDay = stkDay;
	}

	public StkWeekHk getStkWeek() {
		return stkWeek;
	}

	public void setStkWeek(StkWeekHk stkWeek) {
		this.stkWeek = stkWeek;
	}

	public StkWeekHkFwd getStkWeekFwd() {
		return stkWeekFwd;
	}

	public void setStkWeekFwd(StkWeekHkFwd stkWeekFwd) {
		this.stkWeekFwd = stkWeekFwd;
	}

	public StkWeekHkBkw getStkWeekBkw() {
		return stkWeekBkw;
	}

	public void setStkWeekBkw(StkWeekHkBkw stkWeekBkw) {
		this.stkWeekBkw = stkWeekBkw;
	}

	public StkMonthHk getStkMonth() {
		return stkMonth;
	}

	public void setStkMonth(StkMonthHk stkMonth) {
		this.stkMonth = stkMonth;
	}

	public StkMonthHkFwd getStkMonthFwd() {
		return stkMonthFwd;
	}

	public void setStkMonthFwd(StkMonthHkFwd stkMonthFwd) {
		this.stkMonthFwd = stkMonthFwd;
	}

	public StkMonthHkBkw getStkMonthBkw() {
		return stkMonthBkw;
	}

	public void setStkMonthBkw(StkMonthHkBkw stkMonthBkw) {
		this.stkMonthBkw = stkMonthBkw;
	}

	public StkYearHk getStkYear() {
		return stkYear;
	}

	public void setStkYear(StkYearHk stkYear) {
		this.stkYear = stkYear;
	}

	public StkYearHkFwd getStkYearFwd() {
		return stkYearFwd;
	}

	public void setStkYearFwd(StkYearHkFwd stkYearFwd) {
		this.stkYearFwd = stkYearFwd;
	}

	public StkYearHkBkw getStkYearBkw() {
		return stkYearBkw;
	}

	public void setStkYearBkw(StkYearHkBkw stkYearBkw) {
		this.stkYearBkw = stkYearBkw;
	}

}
