package pond.db;

import pond.common.S;
import pond.common.f.Function;

import java.sql.ResultSet;
import java.util.HashMap;
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
   * default pk_provider
   */
  static String defaultPrimaryKey() {
    return S.uuid.vid();
  }

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
    Record t = newValue(recordClass);
    t.setId(defaultPrimaryKey());
    return (T) t;
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
      T t = S.newInstance(recordClass);
      return (T) t.init();
    } catch (InstantiationException | IllegalAccessException e) {
      S._debug(DB.logger, logger -> logger.debug(e.getMessage(),e));
      throw new RuntimeException(e);
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
   * @return E-typed value if get or null
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
   * of argument as defined in declaredFieldNames,
   * WARN: this method change the state of current object
   * rather than return a new copy
   *
   * @param map input query
   * @return altered this
   */
  Record merge(Map<String, Object> map);

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


  default Map<String, Object> view() {
    Map<String, Object> ret = new HashMap<>();
    for (String s : this.fields()) {
      ret.put(s, view(s));
    }
    return ret;
  }

  default Map<String, Object> db() {

    Map<String, Object> ret = new HashMap<>();
    for (String s : this.declaredFieldNames()) {
      ret.put(s, db(s));
    }
    return ret;
  }


}
