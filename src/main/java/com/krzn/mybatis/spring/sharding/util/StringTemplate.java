package com.krzn.mybatis.spring.sharding.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串模板
 *
 * @author yanhuajian
 * @date:2016年4月1日下午8:55:21
 * @version V1.0   
 *
 */
public class StringTemplate {

	/**
	 * 字符串模板替换
	 * 
	 * @param template
	 *            匹配类似velocity规则的字符串%{}
	 * @param tokens
	 *            被替换关键字的的数据源
	 * @return
	 * @author: yanhuajian 2016年4月1日下午8:55:21
	 */
	public static String replace(String template, Map<String, String> tokens) {
		// 生成匹配模式的正则表达式
		String patternString = "\\%\\{(" + join(tokens.keySet().toArray(new Object[] {}), '|') + ")\\}";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(template);

		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String join(Object[] array, char separator) {
		if (array == null) {
			return null;
		}
		int arraySize = array.length;
		int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].toString().length()) + 1) * arraySize);
		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = 0; i < arraySize; i++) {
			if (i > 0) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}
}
