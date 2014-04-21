package com.shuimin.jtiny.codec.model;

//package com.shuimin.jtiny.codec.model;
//
//import com.shuimin.base.S;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map.Entry;
//
//public class SqlHelper {
//
//	//field
//	final String id;
//	final String table;
//
//	StringBuilder head, attrs, where, order, limit, group;
//
//	{
//		head = new StringBuilder("");
//		attrs = new StringBuilder(" * ");
//		where = new StringBuilder(" WHERE ");
//		order = new StringBuilder(" ORDER BY ");
//	}
//
//	public SqlHelper(String id, String table) {
//		this.id = id;
//		this.table = table;
//	}
//
//	public String first() {
//		return "SELECT * FROM " + table + " LIMIT 1 ";
//	}
//
//	public String last() {
//		return "SELECT * FROM " + table + "ORDER BY " + id + " DESC LIMIT 1 ";
//	}
//
//	private String joinArray(String[] arr, String joiner) {
//		return String.join(joiner, arr);
//	}
//
//	public String find(String... id) {
//		return "SELECT * FROM " + table + "WHERE " + id
//				+ " in (" + joinArray(id, ",") + ")";
//	}
//
//	public String limit(int limit) {
//	}
//
//	public String select(String... params) {
//		return null;
//	}
//
//	public String where(String where, String... params) {
//		//TODO
//		return null;
//	}
//
//	public String count(String sql) {
//		int a = sql.indexOf("SELECT ") + 6;
//		int b = sql.indexOf("FROM");
//		String ret = sql.substring(0, a) + " count(*) ct " + sql.substring(b);
//		return ret;
//	}
//
//	private void buildParam(StringBuilder sb, Object o) {
//
//	}
//
//	public String query(Model bo) {
//		StringBuilder ret = new StringBuilder("SELECT ");
//		ret.append(id).append(",");
//		StringBuilder params = new StringBuilder();
//		S._for(bo._fields).each((Entry<String, ModelField> entry) -> {
//			String s = entry.getKey();
//			ret.append(s.toLowerCase());
//			ret.append(",");
//			o = bo.get(s);
//			if (o != null) {
//				params.append("AND ")
//						.append(s)
//						.append("=").append(_wrapValueForSql(o)).append(" ");
//			}
//		});
//		_cleanComma(ret);
//		ret.append(" FROM ");
//		ret.append(((Model) bo).getTable());
//		ret.append(" WHERE 1=1 ");
//		ret.append(params);
//		return ret.toString();
//	}
//
//	private void _cleanComma(StringBuilder sb) {
//		int last = sb.length() - 1;
//		if (sb.charAt(last) == ',') {
//			sb.deleteCharAt(last);
//		}
//	}
//
//	public String getDefaultInsert(BO bo) {
//		StringBuilder ret = new StringBuilder("INSERT INTO ");
//		ret.append(((Model) bo).getTable()).append(" ");
//
//		StringBuilder before = new StringBuilder("(vid,");
//
//		StringBuilder after = new StringBuilder("(");
//		after.append(_wrapValueForSql(bo.getVid())).append(",");
//
//		Object value = null;
//		for (String s : bo.fields()) {
//			value = bo.get(s);
//			if (value == null) {
//				continue;
//			}
//			before.append(s.toLowerCase());
//			before.append(",");
//
//			after.append(_wrapValueForSql(value));
//			after.append(",");
//		}
//		_cleanComma(before);
//		before.append(")");
//		_cleanComma(after);
//		after.append(")");
//
//		ret.append(before).append(" VALUES ").append(after).append(" ");
//		return ret.toString();
//	}
//
//	public String getDefaultUpdate(BO bo) {
//		StringBuilder ret = new StringBuilder("UPDATE ");
//		ret.append(((Model) bo).getTable()).append(" ").append("SET ");
//
//		StringBuilder set = new StringBuilder("vid=");
//		set.append(_wrapValueForSql(bo.getVid()));
//		set.append(",");
//
//		StringBuilder where = new StringBuilder("WHERE vid=");
//		where.append(_wrapValueForSql(bo.getVid()));
//
//		Object value = null;
//		for (String s : bo.fields()) {
//			value = bo.get(s);
//			if (value == null) {
//				continue;
//			}
//			set.append(s.toLowerCase());
//			set.append("=").append(_wrapValueForSql(value));
//			set.append(",");
//		}
//		_cleanComma(set);
//		ret.append(set).append(" ").append(where).append(" ");
//		return ret.toString();
//	}
//
//	public String getDefaultDelete(BO bo) {
//		String vid = bo.getVid();
//		if (vid == null || vid.isEmpty()) {
//			return null;
//		}
//		StringBuilder ret = new StringBuilder("DELETE FROM ");
//		ret.append(((Model) bo).getTable()).append(" ").append("WHERE vid=").append(_wrapValueForSql(vid))
//				.append(" ");
//		return ret.toString();
//	}
//
//	private String _wrapValueForSql(Object value) {
//		Class<?> c = value.getClass();
//		if (String.class.equals(c)) {
//			return "'" + _cleanString(String.valueOf((value))) + "'";
//		}
//		if (Date.class.equals(c)) {
//			return String.valueOf(((Date) value).getTime());
//		}
//		return String.valueOf(value);
//	}
//
//	private String _cleanString(String val) {
//		String ret = val.replaceAll("\\'", "");
//		return ret;
//	}
//
//	/**
//	 * ??????????????????String xxx,xxxx,xx,x,xxx
//	 *
//	 * @param o
//	 * @return
//	 */
//	public String parseArray2String(Object[] os) {
//		String str = "";
//		for (Object o : os) {
//			str += "'" + o + "',";
//		}
//		if (str.endsWith(",")) {
//			str = str.substring(0, str.length() - 1);
//		}
//		return str;
//	}
//
//	/**
//	 * ???ResultSet???????????????????????????????????????????????????????????????
//	 *
//	 * @param rs
//	 * @return
//	 * @throws SQLException
//	 */
//	public static Object[] parseResultSet2Array(ResultSet rs) throws SQLException {
//		if (rs == null) {
//			return new String[0];
//		}
//		List<String> list = new ArrayList<String>();
//		while (rs.next()) {
//			list.add(rs.getString(1));
//		}
//		return list.toArray();
//	}
//
//	/**
//	 * ???????????????????????????bo???vid -----sql??????
//	 *
//	 * sky 2013???9???3???
//	 *
//	 * @param v1 ???????????????????????????
//	 * @param v2 ???????????????????????????
//	 * @param sord false ????????????????????????true ?????????????????????
//	 * ??????true
//	 * @return
//	 */
//	public static String getSearchRSql(String v1, String v2, String val, boolean sord) {
//		String sql = "";
//		if (sord) {
//			sql = "select " + v2 + "_vid from r_" + v1 + "_" + v2 + " where " + v1 + "_vid = '" + val + "'";
//		} else {
//			sql = "select " + v1 + "_vid from r_" + v1 + "_" + v2 + " where " + v2 + "_vid = '" + val + "'";
//		}
//		return sql;
//	}
//
//	public static String getInsertRSql(String v1, String v2, String val1, String val2) {
//		if (StrUtils.notBlank(v1) && StrUtils.notBlank(v2) && StrUtils.notBlank(val1)
//				&& StrUtils.notBlank(val2)) {
//			return "INSERT INTO r_" + v1 + "_" + v2 + "(" + v1 + "_vid," + v2 + "_vid) VALUES('" + val1
//					+ "','" + val2 + "')";
//		}
//		return "";
//	}
//
//	public static String getDeleteRSql(String v1, String v2, String val1,
//			String val2) {
//		if (StrUtils.notBlank(v1) && StrUtils.notBlank(v2)
//				&& StrUtils.notBlank(val1) && StrUtils.notBlank(val2)) {
//			return "DELETE FROM r_" + v1 + "_" + v2 + " WHERE " + v1 + "_vid = '" + val1 + "' AND " + v2 + "_vid = '" + val2 + "'";
//		}
//		if (StrUtils.isBlank(val1)) {
//			return "DELETE FROM r_" + v1 + "_" + v2 + " WHERE " + v2 + "_vid = '" + val2 + "'";
//		}
//		if (StrUtils.isBlank(val2)) {
//			return "DELETE FROM r_" + v1 + "_" + v2 + " WHERE " + v1 + "_vid = '" + val1 + "'";
//		}
//		return "";
//	}
//
////	public static String getDeleteRSql(BO a,BO b)
////	{
////		if (StrUtils.notBlank(v1) && StrUtils.notBlank(v2) && StrUtils.notBlank(val1)
////			&& StrUtils.notBlank(val2)) {
////			return "DELETE FROM r_" + v1 + "_" + v2 + "(" + v1 + "_vid," + v2 + "_vid) VALUES('" + val1
////				+ "','" + val2 + "')";
////		}
////		return "";
////	}
////	
//	/**
//	 * ??????vid??????bo list ------sql
//	 *
//	 * sky 2013???9???3???
//	 *
//	 * @param name ??????
//	 * @param val vid??? xxx,xxx,xxx,xxx
//	 * @return
//	 */
//	public static String getBOSql(String name, String val) {
//		if (StrUtils.isBlank(name) || StrUtils.isBlank(val)) {
//			return null;
//		}
//		return "select * from " + name + " where vid in (" + val + ")";
//
//	}
//
//	/**
//	 * SELECT * FROM A LEFT JOIN R_A_B ON vid = A_VID WHERE B_VID = ''
//	 *
//	 * @author ed
//	 * @param entity
//	 * @return
//	 */
//	public static String getBoLeftJoinR(BO a, BO b) {
//		String sql = SqlHelper.getDefaultQuery(a);
//		StringBuilder sb = new StringBuilder(sql);
//
//		final String a_table_name = ((Model) a).getTable();
//
//		final String b_table_name = ((Model) b).getTable();
//
//		final String a_bo_name = a_table_name.substring(2);
//
//		final String b_bo_name = b_table_name.substring(2);
//
//		final StringBuilder join = new StringBuilder();
//
//		final String R = "r_" + a_bo_name + "_" + b_bo_name;
//
//		join.append("LEFT JOIN ").append(R).append(" ON ").append(" ");
//
//		join.append(BO.VID).append("='").append(a_bo_name).append("_vid").append("'");
//
//		int idx_where = sb.indexOf("WHERE");
//
//		sb.insert(idx_where, join.toString());
//
//		String b_vid = b.getVid();
//		if (StrUtils.notBlank()) {
//			sb.append(" AND ").append(b_bo_name).append("_vid").append("=").append(b_vid);
//		}
//
//		return sb.toString();
//	}
//
////	public static String 
//}
