package com.yiqiniu.mktserver.dao;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.yiqiniu.api.common.service.JobService;
import com.yiqiniu.common.config.DefaultConfig;
import com.yiqiniu.common.mktinfo.service.QuotCommService;
import com.yiqiniu.db4j.ctx.DB;
import com.yiqiniu.mktinfo.persist.tables.StaticImport;
import com.yiqiniu.odps.service.RedisMapService;

@Component
public class MktInfoBeanRepository extends StaticImport {

	@Resource
	protected DB db;
	
	@Resource
	protected RedisMapService redisRpcService;
	
	// 注入配置文件对象，对应项目下：server.properties文件
	@Resource
	protected DefaultConfig defaultConfig;
	
	@Resource
	protected RedisTemplate<String, Object> redisTpl;
	
	@Resource
	protected QuotCommService quotCommService;
	
	@Resource
	protected JobService jobService;

}
