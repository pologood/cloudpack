package com.taobao.hive.udf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import java.util.regex.Pattern;

import java.util.Collection;
import java.util.Set;

public class UDFurlmatch extends UDF {

	private boolean isInited = false;
	
	class RegexMap<V> extends HashMap<Pattern, V> {

		private static final long serialVersionUID = 4761214570434685507L;

		/** ί�е����HashMap */
		protected HashMap<Pattern, V> map = null;

		// ���캯��
		// ----------------------------------------------------------------------

		public RegexMap() {
			super();
			this.map = new HashMap<Pattern, V>();
		}

		public RegexMap(int capacity) {
			super();
			this.map = new HashMap<Pattern, V>(capacity);
		}

		public RegexMap(int capacity, float factor) {
			super();
			this.map = new HashMap<Pattern, V>(capacity, factor);
		}

		public RegexMap(Map<? extends Pattern, ? extends V> map) {
			super();
			this.map = new HashMap<Pattern, V>(map);
		}

		public V get(Object key) {
			if (key != null) {
				for (Map.Entry<Pattern, V> entry : map.entrySet()) {
					if (entry.getKey().matcher(key.toString()).find())
						return entry.getValue();
				}
			}
			return null;
		}

		public int size() {
			return (map.size());
		}

		public boolean isEmpty() {
			return (map.isEmpty());
		}

		public boolean containsKey(Object key) {
			return (map.containsKey(key));
		}

		public boolean containsValue(Object value) {
			return (map.containsValue(value));
		}

		public V put(Pattern key, V value) {
			return (map.put(key, value));
		}

		public void putAll(Map<? extends Pattern, ? extends V> in) {
			map.putAll(in);
		}

		public V remove(Pattern key) {
			return (map.remove(key));
		}

		public void clear() {
			map.clear();
		}

		public Set<Map.Entry<Pattern, V>> entrySet() {
			return map.entrySet();
		}

		public Set<Pattern> keySet() {
			return map.keySet();
		}

		public Collection<V> values() {
			return map.values();
		}

	}

	private RegexMap<String> mapUrl = new RegexMap<String>();
	private Text result = new Text();
	private boolean hdfs1_flag = false;

	public void initHdfsData(String loadpath) throws IOException {
		initHdfs1(loadpath);
	}

	public void initHdfs1(String loadpath) throws IOException {
		if (loadpath != null) {
			loadpath = loadData1(loadpath);
		}
		isInited = true;
		System.out.println("loadpath:" + loadpath);
	}

	public String loadData1(String file) {
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			while (((line = reader.readLine()) != null)) {
				try {
					addUrlLine(line);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return "success";
	}

	private void addUrlLine(String UrlLine) {
		String[] UrlStr = UrlLine.split("\001");
		Pattern p = Pattern.compile(UrlStr[3].trim(), Pattern.MULTILINE);
		mapUrl.put(p, UrlStr[0] + "\001" + UrlStr[2] + "\001" + UrlStr[3]);
		return;
	}

	public Text evaluate(String url, String loadpath) throws IOException{
		if (url == null) {
			return null;
		}
		
		try {
			if (!isInited) {
				initHdfsData(loadpath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		String tmp = mapUrl.get(url);
		if (tmp != null) {
			result.set(tmp.trim());
			return result;
		} else
			return null;

	}

	public static void main(String args[]) {
		UDFurlmatch test = new UDFurlmatch();

		try {
			// System.out.println(test.evaluate("www.taobao.com",
			// "/group/taobao/taobao/dw/stb/20110613/ap_biz_url_dim/dwdbAP_BIZ_URL_DIM-0"));
			System.out.println(test.evaluate("http://tv.taobao.com/item_list.htm", "D:\\text.txt"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
