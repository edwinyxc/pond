package pond.db;

import pond.common.S;
import pond.common.f.Function;

import java.sql.ResultSet;
import java.util.*;

import static pond.common.S._for;
import static pond.common.S._try_ret;

/**
 * Created by ed on 14-5-19.
 * AbstractRecord
 */
public class AbstractRecord extends HashMap<String, Object>
    implements Record {

  DB _db;
  String _tableName = "";
  String idLabel = DEFAULT_PRI_KEY;
  final Field<String> id = new SimpleField<String>(idLabel).init(S.uuid::vid);
  final Field _emptyField = new SimpleField<Void>("null").db(t -> null).view(t -> null);

  Set<Field> declaredFields = new HashSet<>();

  //allow construction
  public AbstractRecord() {
  }

  Function<?, ResultSet> rm;

  private List<Record> others = new ArrayList<>();

  @Override
  public String table() {
    return _tableName;
  }

  @Override
  public Record table(String s) {
    _tableName = s;
    return this;
  }


  @Override
  @SuppressWarnings("unchecked")
  public String id() {
    return this.get(idLabel);
  }

  @Override
  public String idName() {
    return idLabel;
  }

  @Override
  public Set<String> declaredFieldNames() {
    return _for(declaredFields).<String>map(Field::name).toSet();
  }

  @Override
  public Set<Field> declaredFields() {
    return declaredFields;
  }

  @Override
  public Set<String> fields() {
    return this.keySet();
  }

  @Override
  public Record setId(Object pk) {
    if (!declaredFields().contains(id)) {
      declaredFields().add(id);
    }
    this.set(idLabel, pk);
    return this;
  }


  @Override
  public void id(String label) {
    idLabel = label;
    ((SimpleField) id).name = label;

    if (!declaredFields().contains(id)) {
      declaredFields().add(id);
    }
  }


  @SuppressWarnings("unchecked")
  public static class SimpleField<E> implements Field<E> {
    String name;

    Function.F2<E, String, ResultSet> rs_mapper = (name, rs) ->
        _try_ret(() -> (E) rs.getObject(name));

    Function<?, E> view = t -> t;

    Function<?, E> db = t -> t;

    Function<E, ?> validator = t -> (E) t;

    Function.F0 init = () -> null;

    public SimpleField(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public Field<E> mapper(Function.F2<E, String, ResultSet> e) {
      this.rs_mapper = e;
      return this;
    }

    @Override
    public Field<E> init(Function.F0<E> init) {
      this.init = init;
      return this;
    }

    @Override
    public Field<E> merge(Function<E, ?> validator) {
      this.validator = validator;
      return this;
    }

    @Override
    public <V> Field<E> view(Function<V, E> view) {
      this.view = view;
      return this;
    }

    @Override
    public <D> Field<E> db(Function<D, E> data) {
      this.db = data;
      return this;
    }
  }

  @Override
  public Record init() {
    _for(this.declaredFields).each(field -> {
      SimpleField f = (SimpleField) field;
      Object initVal = f.init.apply();
      this.set(f.name, initVal);
    });
    return this;
  }

  @Override
  public <E> Field<E> field(String name) {
    for (Field f : declaredFields) {
      if (f.name().equals(name)) {
        return f;
      }
    }
    // if not found then create one
    Field newField = new SimpleField<>(name);
    declaredFields.add(newField);
    return newField;
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <E> E view(String s) {
    return (E) ((SimpleField) field(s)).view.apply(this.get(s));
  }


  @Override
  @SuppressWarnings("unchecked")
  public Record merge(Map<String, Object> map) {
    Map<String, Object> copy = new HashMap<>(map);
    _for(declaredFields).each(field -> {
      SimpleField f = (SimpleField) field;
      Object o = copy.remove(f.name);
      if (o != null)
        this.set(f.name, f.validator.apply(o));
    });
    this.putAll(copy);
    return this;
  }


  @Override
  @SuppressWarnings("unchecked")
  public final <E> E db(String s) {
    return (E) ((SimpleField) field(s)).db.apply(this.get(s));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E> E get(String s) {
    return (E) super.get(s);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Record set(String s, Object val) {
    super.put(s, val);
    return this;
  }


  @Override
  @SuppressWarnings("unchecked")
  public Function<?, ResultSet> mapper() {
    if (rm == null) {
      rm = rs -> {
        Class<? extends Record> thisClass = this.getClass();
        AbstractRecord r = (AbstractRecord) Record.newValue(thisClass);
        _for(declaredFields).each(f -> {
          SimpleField sf = (SimpleField) f;
          String name = sf.name;
          Object val = sf.rs_mapper.apply(name, rs);
          r.set(name, val);
        });
        return r;
      };
    }
    return this.rm;
  }

  @Override
  public <E> Record mapper(Function<E, ResultSet> mapper) {
    this.rm = mapper;
    return this;
  }


  public Record setInner(String tablename, String colName, Object val) {
    Record record;
    if (null == (record = getInnerRecord(tablename))) {
      record = new AbstractRecord() {
      };
      record.table(tablename);
    }
    record.set(colName, val);
    others.add(record);
    return this;
  }

  public Record getInnerRecord(String tableName) {
    for (Record r : others) {
      if (tableName.equals(r.table())) {
        return r;
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || !(o instanceof AbstractRecord)) {
      return false;
    }
    AbstractRecord other = (AbstractRecord) o;
    Object vid = this.id();
    // if the id is missing, return false
    if (vid == null)
      return false;

    // equivalence by id
    return vid.equals(other.id());
  }

  @Override
  public int hashCode() {
    if (id() != null) {
      return id().hashCode();
    } else {
      return super.hashCode();
    }
  }

}
