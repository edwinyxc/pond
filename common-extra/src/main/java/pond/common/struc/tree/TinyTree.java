package pond.common.struc.tree;

import pond.common.MATRIX;
import pond.common.S;
import pond.common.STRING;
import pond.common.cui.Rect;
import pond.common.cui.RichLayout;
import pond.common.f.Function;
import pond.common.struc.Matrix;

import java.util.*;

public class TinyTree<E> implements Tree<E> {

  private final Map<String, Object> attrs = new TreeMap<>();
  protected Selector<Tree<E>> selector = name -> {
    for (Tree<E> node : TinyTree.this.children) {
      if (node == null) {
        continue;
      }
      if (STRING.notBlank(node.name()) && node.name().equals(name)) {
        return node;
      }
    }
    return null;
  };
  private int idxInParent = -1;
  private E elem;
  private List<Tree<E>> children = new LinkedList<>();
  private Tree<E> parent;
  private Tree<E> root = this;

  protected TinyTree() {
  }

  public TinyTree(E root) {
    this.elem = root;
    this.parent = null;
    name(root.toString());
    this.root = this;
  }

  private TinyTree(Tree<E> tree) {
    this(tree.elem());
    this.children.addAll(tree.children());
  }

  @Override
  public Tree<E> add(Tree<E> t) {
    t.addTo(this);
    return this;
  }

  @Override
  public Tree<E> addTo(Tree<E> t) {
    root = t.root();
    parent = t;
    this.idxInParent = t.children().size();
    t.children().add(this);
    return this;
  }

  @Override
  public Tree<E> after(Tree<E> t) {
    siblings().add(idxInParent + 1, t);
    return this;
  }

  @Override
  public Tree<E> asNew() {
    return new TinyTree<>(this);
  }

  public Object attr(String name) {
    return this.attrs.get(name);
  }

  public Map<String, Object> attrs() {
    return this.attrs;
  }

  public Tree<E> attr(String name, Object o) {
    this.attrs.put(name, o);
    return this;
  }

  @Override
  public Tree<E> before(Tree<E> t) {
    siblings().add(idxInParent, t);
    return this;
  }

  @Override
  public Iterator<Tree<E>> bfs() {
    return new BFS(this);
  }

  @Override
  public List<Tree<E>> children() {
    return children;
  }

  @Override
  public Iterator<Tree<E>> dfs() {
    return new DFS(this);
  }

  @Override
  public E elem() {
    return this.elem;
  }

  @Override
  public Tree<E> elem(E t) {
    this.elem = t;
    return this;
  }

  @Override
  public Tree<E> find(Function<Boolean, Tree<E>> findFunc) {
    Iterator<Tree<E>> bfs = this.bfs();
    while (bfs.hasNext()) {
      final Tree<E> node = bfs.next();
      if (findFunc.apply(node)) {
        return node;
      }
    }
    return null;

  }

  @Override
  public Tree<E> find(String name) {
    Iterator<Tree<E>> bfs = this.bfs();
    while (bfs.hasNext()) {
      final Tree<E> node = bfs.next();
      if (name.equals(node.name())) {
        return node;
      }
    }
    return null;
  }

  @Override
  public Tree<E> first() {
    return children.get(0);
  }

  @Override
  public boolean isFirst() {
    List<?> s = siblings();
    S._assert(s != null && s.size() > 0, "bad logic");
    return this == s.get(0);
  }

  @Override
  public boolean isLast() {
    List<?> s = siblings();
    S._assert(s != null && s.size() > 0, "bad logic");
    return this == s.get(s.size() - 1);
  }

  @Override
  public boolean isLeaf() {
    return children.size() == 0;
  }

  @Override
  public boolean isRoot() {
    return parent == null;
  }

  @Override
  public Tree<E> last() {
    return children.get(children.size() - 1);
  }

  private Matrix _lines() {
    final Matrix view = MATRIX.console(255);
    view.addRow(MATRIX.fromString(this.name()).row(0));
    S._for(children()).each((t) -> {
      String prefix = "┣━━";
      if (t.isLast()) {
        prefix = "┗━━";
      }
      view.addRows(t.isLeaf() ? MATRIX.fromString(prefix, t.name())
                       : MATRIX.addHorizontal(MATRIX.fromString(prefix),
                                              ((TinyTree<E>) t)._lines()));
      for (int i = 1; i < view.rows(); i++) {
        if (view.get(i, 0) == '┗') {
          break;
        } else if (view.get(i, 0) == '┣') {
        } else {
          view.set(i, 0, (int) '┃');
        }
      }
    });
    return view;
  }

  // @Override
  // public Iterator<Tree<E4>> iterator() {
  // if (isLeaf()) {
  // return new NullIterator<Tree<E4>>();
  // }
  // return children.iterator();
  // }
  @Override
  public String name() {
    return (String) this.attr("name");
  }

  @Override
  public Tree<E> name(String name) {
    return this.attr("name", name);
  }

  @Override
  public Tree<E> next() {
    return siblings().get(idxInParent + 1);
  }

  @Override
  public List<Tree<E>> nextAll() {
    return S.array(parent.children())
        .slice(idxInParent + 1, parent.children().size()).toList();
  }

  @Override
  public Tree<E> parent() {
    return parent;
  }

  @Override
  public List<Tree<E>> parents() {
    final List<Tree<E>> ret = new ArrayList<>();
    Tree<E> node = this;
    while (node != null) {
      ret.add(node);
      node = node.parent();
    }
    return ret;
  }

  @Override
  public String[] path() {
    return S._for(parents()).map(a -> (a.name())).joinArray(new String[0]);
  }

  @Override
  public Tree<E> prev() {
    return siblings().get(idxInParent - 1);
  }

  @Override
  public List<Tree<E>> prevAll() {
    return S.array(parent.children()).slice(0, idxInParent).toList();
  }

  @Override
  public void remove() {
    siblings().remove(this);
  }

  // ***test
  @Override
  public Tree<E> remove(Iterable<Tree<E>> t) {

    for (Tree<E> _t : t) {
      children.remove(_t);
    }
    return this;
  }

  @Override
  public Tree<E> remove(Tree<E> t) {
    children.remove(t);
    return this;
  }

  @Override
  public Tree<E> root() {
    return root;
  }

  @Override
  public Tree<E> select(String name) {
    return selector.select(name);
  }

  @Override
  public Tree<E> select(String[] name) {
    Tree<E> cur = this;
    int i = 0;
    while (i < name.length) {
      cur = cur.select(name[i++]);
      if (cur == null) {
        break;
      }
    }
    if (cur == this) {
      return null;
    }
    return cur;
  }

  @Override
  public Selector<Tree<E>> selector() {
    return selector;
  }

  @Override
  public List<Tree<E>> siblings() {
    if (parent == null) {
      return Collections.emptyList();
    }
    return parent.children();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    long s = S.now();
    String ret = "\n" + RichLayout.mkStr(new Rect(_lines()));
    long e = S.now();

    return ret + "\nrendered in " + (e - s) + " ms";

  }

  private class BFS implements Iterator<Tree<E>> {

    private final Queue<Iterator<Tree<E>>> queue = new LinkedList<>();

    public BFS(Tree<E> node) {
      queue.offer(node.children().iterator());
    }

    @Override
    public boolean hasNext() {
      if (queue.isEmpty()) {
        return false;
      }
      Iterator<Tree<E>> it = queue.peek();
      if (it.hasNext()) {
        return true;
      }
      queue.poll();
      return hasNext();
    }

    @Override
    public Tree<E> next() {
      if (hasNext()) {
        Iterator<Tree<E>> it = queue.peek();
        Tree<E> next = it.next();
        if (!next.isLeaf()) {
          queue.offer(next.children().iterator());
        }
        return next;
      }
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "remove not supported, yet.");
    }

  }

  private class DFS implements Iterator<Tree<E>> {

    final private Stack<Iterator<Tree<E>>> stack = new Stack<>();

    public DFS(Tree<E> node) {
      S._assert(node, "node null");
      stack.push(node.children().iterator());
    }

    @Override
    public boolean hasNext() {
      if (stack.isEmpty()) {
        return false;
      }
      Iterator<Tree<E>> it = stack.peek();
      if (it.hasNext()) {
        return true;
      }
      stack.pop();
      return hasNext();
    }

    @Override
    public Tree<E> next() {
      if (hasNext()) {
        Iterator<Tree<E>> it = stack.peek();
        Tree<E> next = it.next();
        if (!next.isLeaf()) {
          stack.push(next.children().iterator());
        }
        return next;
      }
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "remove not supported, yet.");
    }
  }
}
