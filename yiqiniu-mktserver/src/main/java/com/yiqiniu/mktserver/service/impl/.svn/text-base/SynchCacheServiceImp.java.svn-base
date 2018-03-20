package com.yiqiniu.mktserver.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiqiniu.api.mktinfo.util.AggregateOrderBook;
import com.yiqiniu.api.mktserver.HkTsData;
import com.yiqiniu.mktserver.service.SynchCacheService;
import com.yiqiniu.mktserver.util.LocalCache;
import com.yiqiniu.odps.po.Pair;
import com.yiqiniu.odps.service.RedisMapService;

@Service
public class SynchCacheServiceImp implements SynchCacheService {
	private Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Resource
	private RedisMapService redisMapService;
	
	// 单例使用的全局线程池，直到程序被终止时回收，内部不关闭
	private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

	@Override
	public void synchCache() {
		List<Callable<Void>> lstSyncTask = new ArrayList<Callable<Void>>();
		lstSyncTask.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				doSynch(LocalCache.HK_TS_DATA, HkTsData.class, false);
				return null;
			}
		});
		lstSyncTask.add(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				doSynch(LocalCache.AGGREGATE_ORDER_BOOK_MAP, AggregateOrderBook.class, true);
				return null;
			}
		});
		
		List<Future<Void>> lstFuture = null;
		try {
			lstFuture = threadPool.invokeAll(lstSyncTask);
		} catch (InterruptedException e) {
			LOG.error("执行同步本地缓存到redis时遇到中断，忽略...", e);
			return;
		}
		
		if (lstFuture != null) {
			for (Future<Void> future : lstFuture) {
				try {
					future.get();
				} catch (InterruptedException e) {
					LOG.error("获取同步本地缓存结果时遇到中断，忽略...", e);
				} catch (ExecutionException e) {
					LOG.error("同步本地缓存到redis失败", e);
				}
			}
		}
	}

	private <T extends Serializable> void doSynch(Map<String, T> map, Class<T> clazz, boolean isWriteClass) {
		if (map == null)
			return;

		List<Pair> pairs = new ArrayList<Pair>();
		for (Entry<String, T> e : map.entrySet()) {
			Pair pair = new Pair();
			pair.setKey(e.getKey());
			pair.setValue(e.getValue());
			pairs.add(pair);
		}
		
		if (isWriteClass) {
			redisMapService.saveUpdateBatchWriteClass(clazz, pairs);
		} else {
			redisMapService.saveUpdateBatch(clazz, pairs);
		}
	}
}
