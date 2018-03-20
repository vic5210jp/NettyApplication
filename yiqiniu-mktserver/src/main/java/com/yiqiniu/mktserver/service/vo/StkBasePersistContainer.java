/**
 * @Title: StkPersistVO.java
 * @Copyright: © 2015 QiNiu
 * @Company: 深圳齐牛互联网金融服务有限公司
 */

package com.yiqiniu.mktserver.service.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description K线持久化结构，由不复权、前复权、后复权的更新和保存列表组成
 * 
 * @author 余俊斌
 * @date 2015年8月1日 下午4:09:01
 * @version v1.0
 */
public class StkBasePersistContainer<N extends Serializable, F extends Serializable, B extends Serializable> {
	private List<N> nonAdjSaveList;
	private List<N> nonAdjUpdateList;
	private List<F> forwardAdjSaveList;
	private List<F> forwardAdjUpdateList;
	private List<B> backwardAdjSaveList;
	private List<B> backwardAdjUpdateList;

	public StkBasePersistContainer() {
		super();
		nonAdjSaveList = new ArrayList<N>();
		nonAdjUpdateList = new ArrayList<N>();
		forwardAdjSaveList = new ArrayList<F>();
		forwardAdjUpdateList = new ArrayList<F>();
		backwardAdjSaveList = new ArrayList<B>();
		backwardAdjUpdateList = new ArrayList<B>();
	}

	/**
	 * 获取不复权保存列表
	 * @return
	 */
	public List<N> getNonAdjSaveList() {
		return nonAdjSaveList;
	}

	/**
	 * 获取不复权更新列表
	 * @return
	 */
	public List<N> getNonAdjUpdateList() {
		return nonAdjUpdateList;
	}

	/**
	 * 获取前复权保存列表
	 * @return
	 */
	public List<F> getForwardAdjSaveList() {
		return forwardAdjSaveList;
	}

	/**
	 * 获取前复权更新列表
	 * @return
	 */
	public List<F> getForwardAdjUpdateList() {
		return forwardAdjUpdateList;
	}

	/**
	 * 获取后复权保存列表
	 * @return
	 */
	public List<B> getBackwardAdjSaveList() {
		return backwardAdjSaveList;
	}

	/**
	 * 获取后复权更新列表
	 * @return
	 */
	public List<B> getBackwardAdjUpdateList() {
		return backwardAdjUpdateList;
	}
	
}