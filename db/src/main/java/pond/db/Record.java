package pond.db;

import pond.common.JSON;
import pond.common.S;
import pond.common.f.Function;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ed on 2014/4/24.
 * <pre>
 * Record represents record from DB,
 * normally, a row from a table can be mapped into a
 * record using JDBCTmpl#query
 * </pre>
 */
public interface Record {


    interface Field<E> {

        /**
         * Returns the name of this field.
         */
        String name();

        /**
         * Set how the data is getting from the resultSet.
         * function(name,mapper)
         */
        Field<E> mapper(Function.F2<E, String, ResultSet> e);

        /**
         * @param init
         * @return
         */
        Field<E> init(Function.F0<E> init);

        /**
         * set the nil value of the record
         * @param o
         * @return
         */
        Field<E> nil(Object o);

        /**
         * Used for merge (form req, from anything as a query)
         */
        Field<E> merge(Function<E, ?> validator);

        /**
         * Used for view rendering
         */
        <V> Field<E> view(Function<V, E> view);

        /**
         * Used for sql generation.
         */
        <D> Field<E> db(Function<D, E> data);


    }

    Record init();

    /**
     * Try get the Field if not found
     * create new one.
     *
     * @param name field name
     * @return this
     */
    <E> Field<E> field(String name);

    String DEFAULT_PRI_KEY = "id";

    /**
     * Static builder for subclasses
     * Entity is record with its primary key set;
     *
     * @param recordClass sub-class
     * @param <T>         sub-class-type
     * @return new entity
     */
    @SuppressWarnings("unchecked")
    static <T extends Record> T newEntity(Class<T> recordClass) {
        try {
            T t = S.newInstance(recordClass);
            return (T) t.init();
        } catch (InstantiationException | IllegalAccessException e) {
            S._debug(DB.logger, logger -> logger.debug(e.getMessage(), e));
            throw new RuntimeException(e);
        }
    }

    /**
     * Static builder for subclasses
     * Value is record without primary key set;
     *
     * @param recordClass sub-class
     * @param <T>         sub-class-type
     * @return new value OR throw exception when error occurred
     */
    @SuppressWarnings("unchecked")
    static <T extends Record> T newValue(Class<T> recordClass) {
        try {
            T t = (T) ((T) S.newInstance(recordClass)).init();
            t.setId(null);
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            S._debug(DB.logger, logger -> logger.debug(e.getMessage(), e));
            throw new RuntimeException(e);
        }
    }


    static void createTemplateFile(DB db, String absSrcPath, String modelName, String tableName) {
        Map<String, Map<String, Integer>> dbStruc = db.getDbStructures();
        String packageText = modelName.substring(0, modelName.lastIndexOf("."));
        String importText = pond.db.Model.class.getCanonicalName();
        String className = modelName.substring(modelName.lastIndexOf(".") + 1);

        String all = String.format("package %s;\n\nimport %s;\n\npublic class %s extends Model {{ \n%s\n }}",
                packageText, importText, className,
                String.format("\ttable(\"%s\");\n\tid(\"%s\");\n%s\n",
                        tableName,
                        "id",
                        String.join("\n",
                                S._for(
                                        dbStruc.get(tableName).entrySet()
                                ).map(entry ->
                                        String.format("\tfield(\"%s\");", entry.getKey())
                                )
                        )
                )
        );
        try {
            File dir = new File(absSrcPath + File.separator + modelName.substring(0, modelName.lastIndexOf("."))
                    .replace(".", File.separator));
            S.echo("Creating DIR:", dir.getPath());
            if (dir.exists() || dir.mkdirs()) {
                File newFile = new File(dir, modelName.substring(modelName.lastIndexOf(".") + 1) + ".java");
                S.echo("Creating file:", newFile.getPath());

                PrintWriter pw = new PrintWriter(newFile, "UTF-8");
                pw.print(all);
                pw.flush();

                S.echo("File Content:", all);
                pw.close();
            } else {
                S.echo("OPEN FILE ERROR");
                //error
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * declared field-names that should be same as declared in db
     * readonly
     *
     * @return
     */
    Set<String> declaredFieldNames();

    /**
     * declared fields that should be same as declared in db
     *
     * @return
     */
    Set<Field> declaredFields();

    /**
     * Returns all fields
     *
     * @return
     */
    Set<String> fields();

    /**
     * get primary key -value
     *
     * @return value
     */
    <E> E id();

    /**
     * set primary key
     *
     * @param pk id -value
     * @return this
     */
    public Record setId(Object pk);

    /**
     * get primary key -name
     *
     * @return this
     */
    public String idName();

    /**
     * set id
     *
     * @param keyName primary key name
     */
    void id(String keyName);

    /**
     * get table -name
     *
     * @return this
     */
    public String table();

    /**
     * set table -name
     *
     * @param s table-name
     * @return this
     */
    public Record table(String s);

    /**
     * view a value ( entity -> view )
     */
    <E> E view(String s);

    /**
     * db a value ( entity -> db )
     */
    <E> E db(String s);

    /**
     * get value
     *
     * @param s   name
     * @param <E> return type
     * @return E4-typed value if get or null
     */
    <E> E get(String s);

    /**
     * set value
     *
     * @param s   name
     * @param val value
     * @return this
     */
    Record set(String s, Object val);

    /**
     * Merge parameters to the underlying record.
     * WARN: this method change the state of current object
     * rather than return a new copy
     *
     * @param map input query
     * @return merged record
     */
    Record merge(Map<String, Object> map);

    /**
     * Merge parameters to the underlying record,
     * <strong>except for the id field</strong>.
     * <p>
     * WARN: this method change the state of current object
     * rather than return a new copy
     *
     * @param map input query
     * @return merged record
     */
    Record mergeExceptId(Map<String, Object> map);

    /**
     * get defined RowMapper
     *
     * @param <E> rs_mapper-to type
     * @return rowMapper
     */
    <E> Function<E, ResultSet> mapper();

    /**
     * set rowMapper
     *
     * @param mapper rs_mapper to-set
     * @param <E>    rs_mapper-to type
     * @return this
     */
    <E> Record mapper(Function<E, ResultSet> mapper);


    /**
     * @return a hash map contains all the current entries (this.fields) mapped with the Field::view function
     */
    default Map<String, Object> view() {
        Map<String, Object> ret = new HashMap<>();
        for (String s : this.fields()) {
            ret.put(s, view(s));
        }
        return ret;
    }

    /**
     * * @return the underlying record as a map
     */
    @SuppressWarnings("unchecked")
    default Map<String, Object> toMap() {
        if (this instanceof Map) {
            return (Map<String, Object>) this;
        }
        Map<String, Object> ret = new HashMap<>();
        for (String s : this.fields()) {
            ret.put(s, this.get(s));
        }
        return ret;
    }

    /**
     * @return a hash map contains all the pre-defined entries (this.declaredFieldNames) mapped with the Field::db function
     */
    default Map<String, Object> db() {
        Map<String, Object> ret = new HashMap<>();
        for (String s : this.declaredFieldNames()) {
            ret.put(s, db(s));
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    static <T extends Record> T newFromJSON(Class<T> eClass, String jsonObject) {
        return (T) newEntity(eClass).mergeExceptId(JSON.parse(jsonObject));
    }

    @SuppressWarnings("unchecked")
    static <T extends Record> List<T> newArrayFromJSON(Class<T> tClass, String jsonArray) {
        List<Map<String, Object>> arr = JSON.parseArray(jsonArray);
        return S._for(arr).map(m ->
                (T) Record.newEntity(tClass).mergeExceptId(m).toMap()
        ).toList();
    }


}
