/**
 * @Title: RefreshAdjustFactorServiceImpl.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.api.mktinfo.vo.LatestAdjustFactorVO;
import com.yiqiniu.api.mktinfo.vo.QuotationVO;
import com.yiqiniu.api.mktinfo.vo.StkDailyVO;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.common.utils.ArithmeticUtil;
import com.yiqiniu.mktinfo.persist.po.HkAssetInfo;
import com.yiqiniu.mktserver.dao.AssetInfoDao;
import com.yiqiniu.mktserver.dao.MktInfoBeanRepository;
import com.yiqiniu.mktserver.dao.StkHisQuoteDao;
import com.yiqiniu.mktserver.service.RefreshAdjustFactorService;
import com.yiqiniu.odps.po.Pair;

/**
 * @description 刷新复权因子相关服务实现
 * 
 * @author 余俊斌
 * @date 2015年7月30日 下午7:15:34
 * @version v1.0
 */
@Service
public class RefreshAdjustFactorServiceImpl extends MktInfoBeanRepository implements
		RefreshAdjustFactorService {
	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Resource
	private AssetInfoDao assetInfoDao;
	@Resource
	private StkHisQuoteDao stkHisQuoteDao;

	@Override
	public void refreshStkAF(String date) {
		// 查询当前有效的资产信息
		List<HkAssetInfo> lstAssetInfo = assetInfoDao.findAllAssetInfo();
		// 查询所有个股最近一条日K
		Map<String, StkDailyVO> mapLastStkDaily = assetInfoDao.findLastStkDailyBeforeTime(date);
		// 获取个股行情
		Map<String, QuotationVO> mapQuotation = new HashMap<String, QuotationVO>();
		try {
			Map<String, HkTsData> mapShQuotation = redisRpcService.findAllObject(HkTsData.class);
			mapQuotation.putAll(mapShQuotation);
		} catch (Exception e) {
			LOG.error("刷新复权因子 -- 从redis获取港交所行情数据异常", e);
		}

		// 计算最新复权因子
		List<Pair> lstAfToBeSave = new ArrayList<Pair>();
		for (HkAssetInfo assetInfo : lstAssetInfo) {
			try {
				String assetId = assetInfo.getAssetId();

				double af = -1; // 最后如果出现-1的复权因子，肯定是处理错误，如果是0，则是计算所得，此处用于辅助排错
				if (assetInfo.getIsStatus()) {
					// 如果有效/尚未退市，则正常处理
					if (!mapQuotation.containsKey(assetId)) {
						// 如果行情中没有数据，则跳过
						LOG.debug("{}未上市或者已退市，无当日行情，", assetId);
						continue;
					}

					if (mapLastStkDaily.containsKey(assetId)) {
						QuotationVO quotationVO = mapQuotation.get(assetId);
						StkDailyVO stkDaily = mapLastStkDaily.get(assetId);
						// 如果有T-1的日K，则按公式计算新的af
						// 港股会有昨收为0，或者T-1收盘为0的情况，此时沿用T-1日的复权因子
						if (quotationVO.getPrevClose() == 0 || stkDaily.getClose() == 0) {
							af = stkDaily.getAdjFactor();
						} else {
							// 如果最近一条日K的日期和行情在同一天（比如收盘后重跑），则取日K的除权因子
							// 【调用此方法的任务需要等待实时行情任务完成，故不需要考虑开盘前，行情与日K同一天的情况】
							if (StringUtils.equals(stkDaily.getDate(), quotationVO.getDate())) {
								af = stkDaily.getAdjFactor();
							} else {
								// T日复权因子 AF ＝T-1日收盘价/T日昨收盘价× T-1日 AF
								af = ArithmeticUtil.mul(
										ArithmeticUtil.div(stkDaily.getClose(),
												quotationVO.getPrevClose()), stkDaily.getAdjFactor());
							}
						}
					} else {
						// 如果没有T-1的日K，则为新上市，af应取1
						af = 1;
					}
				} else {
					// 如果已退市，则用最后一条日K的复权因子，如果没有日K，则取1
					if (mapLastStkDaily.containsKey(assetId)) {
						StkDailyVO stkDaily = mapLastStkDaily.get(assetId);
						af = stkDaily.getAdjFactor();
					} else {
						LOG.warn("{}已退市，且没有日K数据，复权因子取1", assetId);
						af = 1;
					}
				}

				LatestAdjustFactorVO afVO = new LatestAdjustFactorVO();
				afVO.setAssetId(assetId);
				afVO.setAdjustFactor(af);

				Pair pair = new Pair();
				pair.setKey(assetId);
				pair.setValue(afVO);
				lstAfToBeSave.add(pair);
			} catch (Exception e) {
				LOG.error("生成最新的复权因子异常: assetId=" + assetInfo.getAssetId(), e);
			}
		}

		// 刷新保存复权调整因子
		LOG.info("更新redis...");
		redisRpcService.delete(LatestAdjustFactorVO.class);
		redisRpcService.saveUpdateBatch(LatestAdjustFactorVO.class, lstAfToBeSave);
		LOG.info("完成刷新复权因子");
	}

	@Override
	public void refreshForwardAdjust(String date) {
		// 查询当前有效的资产信息
		List<HkAssetInfo> lstAssetInfo = assetInfoDao.findAllAssetInfo();
		// 查询所有个股最近一条日K
		Map<String, StkDailyVO> mapLastStkDaily = assetInfoDao.findLastStkDailyBeforeTime(date);
		// 获取个股行情
		Map<String, QuotationVO> mapQuotation = new HashMap<String, QuotationVO>();
		try {
			Map<String, HkTsData> mapShQuotation = redisRpcService.findAllObject(HkTsData.class);
			mapQuotation.putAll(mapShQuotation);
		} catch (Exception e) {
			LOG.error("从redis获取港交所行情数据异常", e);
		}

		// 遍历资产，刷新
		for (HkAssetInfo assetInfo : lstAssetInfo) {
			String assetId = assetInfo.getAssetId();
			// 退市个股不刷新
			if (!assetInfo.getIsStatus()) {
				continue;
			}

			try {
				if (!mapQuotation.containsKey(assetId)) {
					// 如果行情中没有数据，则跳过
					LOG.warn("{}未上市，无当日行情，", assetId);
					continue;
				}

				if (mapLastStkDaily.containsKey(assetId)) {
					QuotationVO quotationVO = mapQuotation.get(assetId);
					StkDailyVO stkDaily = mapLastStkDaily.get(assetId);

					// 如果最近一条日K的日期和行情在同一天（比如收盘后重跑），则不刷新
					if (StringUtils.equals(stkDaily.getDate(), quotationVO.getDate())) {
						continue;
					}

					// 只对除权的资产进行刷新
					if (quotationVO.getPrevClose() != stkDaily.getClose()
							&& !assetId.contains("IDX")) {
						LatestAdjustFactorVO adjFactorVO = redisRpcService.findObject(
								LatestAdjustFactorVO.class, assetId);
						if (adjFactorVO == null) {
							LOG.error("未取到最新的复权调整因子，不能进行周、月、年K的刷新，assetId={}", assetId);
							continue;
						}

						// 刷新周、月、年K的前复权数据
						stkHisQuoteDao.refreshFowardAdjust(assetId, adjFactorVO.getAdjustFactor());
					}
				} // else：没有T-1的日K，则为新上市，不需要刷新
			} catch (Exception e) {
				LOG.error(assetInfo.getAssetId() + "刷新前复权失败", e);
			}
		}
	}

}
