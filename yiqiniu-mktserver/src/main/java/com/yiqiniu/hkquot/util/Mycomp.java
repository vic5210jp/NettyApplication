package com.yiqiniu.hkquot.util;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//比较器 xiongpan
public class Mycomp implements Comparator<String> {
	private static Logger LOG = LoggerFactory.getLogger(Mycomp.class);
	@Override 
	public int compare(String o1, String o2) throws NumberFormatException{
		int a = 0;
		int b = 0;
		try {
			a = Integer.parseInt(o1.substring(0, o1.lastIndexOf("-")));
			b = Integer.parseInt(o2.substring(0, o2.lastIndexOf("-")));
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("String转化int异常！");
		}
		return b-a;
	}
}
