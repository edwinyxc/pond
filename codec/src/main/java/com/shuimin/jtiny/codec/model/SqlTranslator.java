package com.shuimin.jtiny.codec.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* Created by ed on 2014/4/11.
*/
public class SqlTranslator {
    static String COUNT = "SELECT COUNT(*) FROM";

    static String INSERT = "INSERT INTO ";

    static String VALUES = " VALUES";

    static String DELETE = "DELETE FROM ";

    static String UPDATE = "UPDATE ";

    static String SET = " SET";

    static String SELECT = "SELECT ";

    static String FROM = " FROM ";

    static String WHERE = " WHERE";

    static String AND = " AND";

    static String LIMIT = " LIMIT ";

    static String OrderBy = " ORDER BY ";

    static String GroupBy = " GROUP BY ";

    static String HAVING = " HAVING ";

    static String IN = " IN ";

//	static String END = ";";

    StringBuilder result = new StringBuilder();

    String tableN="";

    Map mapIn;

    ArrayList ls = new ArrayList();

    public SqlTranslator(String tableN){
        this.tableN = tableN;
    }

    public SqlTranslator(String tableN,Map<String,Object> mapIn){
        this.tableN = tableN;
        this.mapIn = mapIn;
        System.out.println("构造完成");
        mapToList();
    }

    public void mapToList(){
        for (Object key : mapIn.keySet()) {
            ls.add(key);
            System.out.println("key= "+ key + " and value= " + mapIn.get(key));
        }
    }

    //INSERT INTO 表名称 VALUES (值1, 值2,....)
    //INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
    public StringBuilder insert(){
        result.append(INSERT+tableN);
        StringBuilder tempK = new StringBuilder();
        StringBuilder tempV = new StringBuilder();
        Iterator it = ls.iterator();
        while(it.hasNext()){
            Object tempD = it.next();
            tempK.append(tempD);
            if(it.hasNext()){
                tempK.append(",");
            }
            tempV.append("'"+mapIn.get(tempD)+"'");
            if(it.hasNext()){
                tempV.append(",");
            }
        }
        result.append("("+tempK+")").append(VALUES).append("("+tempV+")");
//		.append(END)
        return result;
    }

    //DELETE FROM user WHERE 1='value1';
    public StringBuilder delete(){
        result.append(DELETE+tableN+WHERE);
        System.out.println("map没有传入参数");
        Iterator it = ls.iterator();
        while(it.hasNext()){
            Object tempD = it.next();
            result.append(" "+tempD+"="+"'"+mapIn.get(tempD)+"'");
            if(it.hasNext()){
                result.append(AND);
            }
        }
//		result.append(END);
        return result;
    }

    public StringBuilder delete(String tableN,Map<String,Object> map){
        Iterator it = ls.iterator();
        while(it.hasNext()){
            Object tempD = it.next();
            result.append(" "+tempD+"="+mapIn.get(tempD));
            if(it.hasNext()){
                result.append(AND);
            }
        }
//		result.append(END);
        return result;
    }

    //UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值
    //update tableName SET columnName1='0',columnName2='',columnName3='0' where columnName4='abc';
    public StringBuilder update(String vid,String id){
        result.append(UPDATE+tableN+SET);
        Iterator it = ls.iterator();
        while(it.hasNext()){
            Object tempD = it.next();
            result.append(" "+tempD+"="+"'"+mapIn.get(tempD)+"'");
            if(it.hasNext()){
                result.append(",");
            }
        }
        result.append(WHERE+"("+vid+"='"+id+"')");
//		+END
        return result;
    }

    //SELECT COUNT(column_name) FROM table_name;
    public StringBuilder count(){
        result.append(SELECT);
        return result;
    }
    /**
     *
     * @return
     */
    public SqlTranslator select(){
        result.append(SELECT+"*"+FROM+tableN+WHERE);
        Iterator it = ls.iterator();
        if(it.hasNext()){
        }
        while(it.hasNext()){
            Object tempD = it.next();
            result.append(" "+tempD+"="+"'"+mapIn.get(tempD)+"'");
            if(it.hasNext()){
                result.append(AND);
            }
        }
//		result.append(END);
        return this;
    }


    //SqlTranslator("user");
    public SqlTranslator select(String columnN){
        Iterator it = ls.iterator();
        if(!it.hasNext()){
            result.append(SELECT+columnN+FROM+tableN+WHERE);
//			+END
            return this;
        }
        result.append(SELECT+columnN+FROM+tableN+WHERE);
        while(it.hasNext()){
            Object tempD = it.next();
            result.append(" "+tempD+"="+"'"+mapIn.get(tempD)+"'");
            if(it.hasNext()){
                result.append(AND);
            }
        }
//		result.append(END);
        return this;
    }

    public SqlTranslator limit(int start,int offset){
        result.append(LIMIT+start+","+offset);
        return this;
    }

    public SqlTranslator limit(int total){
        result.append(LIMIT+total);
        return this;
    }

    public SqlTranslator orderBy(String order){
        result.append(OrderBy+order);
        return this;
    }

    public SqlTranslator having(String condition){
        result.append(HAVING+condition);
        return this;
    }

    //SELECT * FROM PersonsWHERE LastName IN ('Adams','Carter');
    public SqlTranslator in(String condition,String[] aa){
        StringBuilder tempA = new StringBuilder();
        for(int i=0;i<aa.length;i++){
            tempA.append("'"+aa[i]+"'");
            if(i<aa.length-1){
                tempA.append(",");
            }
        }
        result.append(" "+condition).append(IN).append("("+tempA+")");
        return this;
    }

    public String toString(){
        return result.toString();
    }

    //清除分号，默认执行5次 最后append上分号;
//	public StringBuilder clearSem(){
//		int tempI = result.indexOf(END);
//		result.deleteCharAt(tempI);
//		result.append(END);
//		return result;
//	}

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", "value1");
        map.put("2", "value2");
        map.put("3", "value3");
        String[] aa={"1","2","3","4"};
        SqlTranslator sqlt = new SqlTranslator("user");
        System.out.println(sqlt.select("*").in("hurry",aa).having("SUM>=0"));
    }
}
