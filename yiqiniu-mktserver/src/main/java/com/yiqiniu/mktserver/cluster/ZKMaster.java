/*
 * FileName: ZKCluster.java
 * Copyright: Copyright 2014-12-20 Yiqiniu Tech. Co. Ltd.All right reserved.
 * Description: 
 *
 */
package com.yiqiniu.mktserver.cluster;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

import com.yiqiniu.common.config.DefaultConfig;
/**
 * <code>ZKCluster<code> 主从关系的简单实现
 *
 * @author Jimmy
 * @since  Yiqiniu v0.0.1 (2014-12-20)
 *
 */
// @Component
public class ZKMaster implements Watcher{
	protected static final Logger L = Logger.getLogger(ZKMaster.class);
	
	public static final String ENCODING = "UTF-8";
	/** 根目录 */
	public String rootPath;
	/** 服务器列表目录 */
	public String server_list;
	/** 记录主机的目录 */
	public String master;
	@Resource
	private DefaultConfig defConfig;
	/** 节点名字 */
	private String seqName;
	/**  */
	private String seqNamePart;
	/**  */
	private ZooKeeper zk;
	/** 是否是主机 */
	private boolean isMaster = true;
	/**  */
	CountDownLatch cdl = new CountDownLatch(1);
	/** 是否是ZK地址 */
	private String zkHost;
	/** 连接ZK最长等待的时间 */
	private long lWaitTime;
	
	@PostConstruct
	protected void preInit() {
		
		/*zk.host=192.168.1.180:2181
				seq.name=mktrtclient
				root.path=mktrtserver*/
				
		seqNamePart = defConfig.getVal("seq.name");
		zkHost = defConfig.getVal("zk.host");
		rootPath = "/" + defConfig.getVal("root.path");
		server_list = rootPath + "/serverlist";
		master = rootPath +  "/master";
		lWaitTime = 12000;
		
		//System.out.println("server_list---"+server_list);
		try {
			L.info("开始连接到ZK");
			createZK(sessionWatcher);
		} catch (Exception e) {
			L.error("连接到ZK异常", e);
		}
	}
	
	/**
	 * @param sessionWatch
	 * @return
	 * @throws Exception
	 */
	public ZooKeeper createZK(Watcher sessionWatch) throws Exception {
		cdl = new CountDownLatch(1);
		zk = new ZooKeeper(zkHost, 3000 , sessionWatch);
		
		//System.out.println("cdl---"+cdl);
		try {
			cdl.await(lWaitTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			L.warn("CountDownLatch await异常", e);
		}
		if (!States.CONNECTED.equals(zk.getState())) {
			L.info("连接到ZK失败或超时 :" + zkHost);
			zk.close();
		} else {
			processSuccess();
		}
		return zk;
	}
	
	private Watcher sessionWatcher = new Watcher() {
		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				cdl.countDown();
			}
			if (event.getState() == KeeperState.Expired) {
				// 过期， 重连
				close();
				try {
					createZK(this);
				} catch (Exception e) {
					L.error("createZK 异常", e);
				}
			}
		}
	};
	/**
	 * 
	 */
	public void close() {
		try {
			zk.close();
			zk = null;
		} catch (InterruptedException e) {
			L.error("关闭ZK， 异常", e);
		}
	}
	/**
	 * 是否是Master
	 * @return
	 */
	public boolean isMaster() {
		return isMaster;
	}
	/**
	 * 增加节点的监听器
	 */
	public void addWatch() {
		if (zk == null) {
			L.error("Zookeeper 异常");
		} else {
			try {
				zk.getChildren(server_list, this);
			} catch (Exception e) {
				L.error("增加监听器异常", e);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		findMaster();
		// 重新增加监听器
		addWatch();
	}
	/**
	 * 查找Master, 序列号最小的为Master
	 */
	public void findMaster() {
		try {
			List<String> lstNums = zk.getChildren(server_list, null);
			if (!CollectionUtils.isEmpty(lstNums)) {
				String sMaster = null;
				int iSeqLen = seqNamePart.length() + 2;
				long lMin = -1;
				for (String sNum : lstNums) {
					String str = sNum.substring(iSeqLen);
					long lNum = Long.parseLong(str);
					if (lMin == -1) {
						lMin = lNum;
						sMaster = sNum.substring(0, iSeqLen);
					}
					if (lNum < lMin) {
						sMaster = sNum.substring(0, iSeqLen);
					}
				}
				String serverMaster = null;
				if (zk.exists(master, null) != null) {
					byte[] bb = zk.getData(master, null, null);
					serverMaster = new String(bb, ENCODING);
				}
				if (serverMaster != null) {
					L.info("Master是：" + serverMaster + ", 本节点是:" + seqName);
					isMaster = sMaster.equals(seqName);
					return ;
				}
				L.info("在" + rootPath + "中选出来新的Master的节点名: " + sMaster);
				if (seqName.equals(sMaster)) {
					// 判断是否已经存在master
					Stat stat = zk.exists(master, true);
					if (stat == null) {
						zk.create(master, seqName.getBytes(ENCODING), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
						isMaster = true;
					}
				} else {
					isMaster = false;
				}
			}
		} catch (Exception e) {
			L.error("获取子节点列表异常", e);
		}
	}
	/**
	 * 连接ZK成功后的处理
	 * @throws UnsupportedEncodingException
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public void processSuccess() throws UnsupportedEncodingException, KeeperException, InterruptedException {
		L.info("连接到ZK成功.");
		// 如果不存在根目录, 则创建根目录
		if (zk.exists(rootPath, true) == null) {
			zk.create(rootPath, "root".getBytes(ENCODING), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// 如果不存在server_list目录, 就创建server_list目录
		if (zk.exists(server_list, true) == null) {
			zk.create(server_list, "master-slaver".getBytes(ENCODING), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// 增加监听器
		addWatch();
		zk.exists(server_list, null);
		createSelfSeq();
	}
	/**
	 * 创建自身的节点, 所有的节点都是seqName + 两位数字
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void createSelfSeq() throws KeeperException, InterruptedException {
		boolean flag = false;
		// 尝试2次
		for (int i = 0; i < 2; i++) {
			for (int iNum = 1; iNum < 99; iNum++) {
				String seqNameTmp = seqNamePart + String.format("%02d", iNum);
				String path = server_list + "/" + seqNameTmp ;
				List<String> lstChildren = zk.getChildren(server_list, null);
				if (!isContainsSeq(lstChildren, seqNameTmp)) {
					try {
						String str = zk.create(path, seqNameTmp.getBytes(ENCODING),
								Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
						L.info("有序列号的节点名字:" + str);
						flag = true;
						seqName = seqNameTmp;
						L.info("增加子节点成功， 节点名: " + seqName);
						break;
					} catch (Exception e) {
						L.error("增加子节点异常 ", e);
					}
				}
			}
			if (flag) {
				break;
			}
		}
	}
	/**
	 * 
	 * @param lstChildren
	 * @param seqName
	 * @return
	 */
	public boolean isContainsSeq(List<String> lstChildren, String seqName) {
		if (CollectionUtils.isEmpty(lstChildren)) {
			return false;
		}
		for (String str : lstChildren) {
			if (str.startsWith(seqName)) {
				return true;
			}
		}
		return false;
	}
}
