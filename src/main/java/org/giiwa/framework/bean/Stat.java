/*
 * Copyright 2015 JIHU, Inc. and/or its affiliates.
 *
*/
package org.giiwa.framework.bean;

import org.giiwa.core.bean.Bean;
import org.giiwa.core.bean.BeanDAO;
import org.giiwa.core.bean.Beans;
import org.giiwa.core.bean.Column;
import org.giiwa.core.bean.Helper.V;
import org.giiwa.core.bean.Helper.W;
import org.giiwa.framework.web.Language;
import org.giiwa.core.bean.Table;
import org.giiwa.core.bean.UID;
import org.giiwa.core.bean.X;

/**
 * The Class Stat is used to stat utility and persistence.
 * 
 * @author wujun
 *
 */
@Table(name = "gi_stat")
public class Stat extends Bean implements Comparable<Stat> {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	public static final BeanDAO<Stat> dao = BeanDAO.create(Stat.class);

	public static final String SIZE_MIN = "min";
	public static final String SIZE_HOUR = "hour";
	public static final String SIZE_DAY = "day";
	public static final String SIZE_WEEK = "week";
	public static final String SIZE_MONTH = "month";
	public static final String SIZE_SEASON = "season";
	public static final String SIZE_YEAR = "year";

	public static final String TYPE_DELTA = "delta";
	public static final String TYPE_SNAPSHOT = "snapshot";

	@Column(name = X.ID)
	protected long id; // 日期

	@Column(name = "module")
	protected String module; // 统计模块

	@Column(name = "date")
	protected String date; // 日期

	@Column(name = "size")
	protected String size;
	// min, hour, day, week,month, year

	public String getDate() {
		return date;
	}

	public String getModule() {
		return module;
	}

	/**
	 * Insert or update.
	 *
	 * @param module
	 *            the module
	 * @param date
	 *            the date
	 * @param size
	 *            the size
	 * @param n
	 *            the n
	 * @return the int
	 */
	public static int insertOrUpdate(String module, String date, String size, V v, long... n) {
		if (v == null) {
			v = V.create();
		} else {
			v = v.copy();
		}
		
		try {
			W q = W.create().copy(v).and("date", date).and("size", size).and("module", module);

			if (!dao.exists(q)) {
				
				long id = UID.next("stat.id");
				while (dao.exists(id)) {
					id = UID.next("stat.id");
				}
				v.append("date", date).force(X.ID, id).append("size", size).append("module", module);
				for (int i = 0; i < n.length; i++) {
					v.set("n" + i, n[i]);
				}

				return dao.insert(v);

			} else {
				/**
				 * only update if count > original
				 */
				for (int i = 0; i < n.length; i++) {
					v.set("n" + i, n[i]);
				}
				return dao.update(q, v);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Stat o) {
		if (this == o)
			return 0;

		int c = getDate().compareTo(o.getDate());
		return c;
	}

	/***
	 * @deprecated
	 * @param name
	 * @param size
	 * @param n
	 */
	public static void log(String name, String size, long... n) {
		snapshot(name, size, null, n);
	}

	public static void delta(String name, String[] sizes, V v, long... n) {
		if (sizes != null) {
			for (String size : sizes) {
				delta(name, size, v, n);
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @param size
	 * @param n
	 */
	public static void delta(String name, String size, V v, long... n) {
		Language lang = Language.getLanguage();

		String date = null;

		if (X.isSame(size, Stat.SIZE_MIN)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMddHHmm");
		} else if (X.isSame(size, Stat.SIZE_HOUR)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMddHH");
		} else if (X.isSame(size, Stat.SIZE_DAY)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMdd");
		} else if (X.isSame(size, Stat.SIZE_WEEK)) {
			date = lang.format(System.currentTimeMillis(), "yyyyww");
		} else if (X.isSame(size, Stat.SIZE_MONTH)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMM");
		} else if (X.isSame(size, Stat.SIZE_SEASON)) {
			int season = X.toInt(lang.format(System.currentTimeMillis(), "MM")) / 3 + 1;
			date = lang.format(System.currentTimeMillis(), "yyyy") + "0" + season;
		} else if (X.isSame(size, Stat.SIZE_YEAR)) {
			date = lang.format(System.currentTimeMillis(), "yyyy");
		} else {
			log.error("not support the [" + size + "], supported: min, hour, day, month, year");
			return;
		}

		W q = W.create().copy(v);

		Stat s1 = dao.load(q.and("module", name + "." + Stat.TYPE_SNAPSHOT).and("size", size)
				.and("date", date, W.OP.neq).sort("created", -1));
		long[] d = new long[n.length];
		for (int i = 0; i < d.length; i++) {
			d[i] = s1 == null ? n[i] : n[i] + s1.getLong("n" + i);
		}
		Stat.insertOrUpdate(name + "." + Stat.TYPE_SNAPSHOT, date, size, v, d);
		Stat.insertOrUpdate(name + "." + Stat.TYPE_DELTA, date, size, v, n);

	}

	public static void snapshot(String name, String[] sizes, V v, long... n) {
		if (sizes != null) {
			for (String size : sizes) {
				snapshot(name, size, v, n);
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @param size
	 * @param n
	 */
	public static void snapshot(String name, String size, V v, long... n) {
		Language lang = Language.getLanguage();

		String date = null;

		if (X.isSame(size, Stat.SIZE_MIN)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMddHHmm");
		} else if (X.isSame(size, Stat.SIZE_HOUR)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMddHH");
		} else if (X.isSame(size, Stat.SIZE_DAY)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMMdd");
		} else if (X.isSame(size, Stat.SIZE_WEEK)) {
			date = lang.format(System.currentTimeMillis(), "yyyyww");
		} else if (X.isSame(size, Stat.SIZE_MONTH)) {
			date = lang.format(System.currentTimeMillis(), "yyyyMM");
		} else if (X.isSame(size, Stat.SIZE_SEASON)) {
			int season = X.toInt(lang.format(System.currentTimeMillis(), "MM")) / 3 + 1;
			date = lang.format(System.currentTimeMillis(), "yyyy") + "0" + season;
		} else if (X.isSame(size, Stat.SIZE_YEAR)) {
			date = lang.format(System.currentTimeMillis(), "yyyy");
		} else {
			log.error("not support the [" + size + "], supported: min, hour, day, month, year");
			return;
		}

		W q = W.create().copy(v);
		Stat s1 = dao.load(
				q.and("module", name + ".snapshot").and("size", size).and("date", date, W.OP.neq).sort("created", -1));
		long[] d = new long[n.length];
		for (int i = 0; i < d.length; i++) {
			d[i] = s1 == null ? n[i] : n[i] - s1.getLong("n" + i);
		}
		Stat.insertOrUpdate(name + "." + Stat.TYPE_SNAPSHOT, date, size, v, n);
		Stat.insertOrUpdate(name + "." + Stat.TYPE_DELTA, date, size, v, d);

	}

	public static Beans<Stat> load(String name, String type, String size, W q, int s, int n) {
		if (q == null) {
			q = W.create();
		}
		q.and("module", name + "." + type).and("size", size);

		Beans<Stat> bs = dao.load(q, s, n);
		if (bs != null && !bs.isEmpty()) {
			String date = null;
			Stat e = null;
			for (int i = 0; i < bs.size(); i++) {
				e = bs.get(i);
				e.cut(date, size);
				date = e.date;
			}
			e.set("shortdate", e.date);
		}
		return bs;
	}

	private void cut(String date, String size) {
		String shortdate = this.date;
		if (!X.isEmpty(date)) {
			int len = Math.min(date.length(), shortdate.length()) - 2;
			while (len > 0) {
				String s = date.substring(0, len);
				if (shortdate.startsWith(s)) {
					shortdate = shortdate.substring(len);
					break;
				}
				len -= 2;
			}
		}
		this.set("shortdate", shortdate);
	}
}
