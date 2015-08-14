package pond.common.struc.tree;

import pond.common.f.Function;

import java.util.Iterator;
import java.util.List;

public interface Tree<T> {

  String name();

  Tree<T> name(String name);

  /**
   * <p>
   * Get the element of current tree node
   * </p>
   *
   * @return
   */
  T elem();

  /**
   * <p>
   * Set the element of current tree node
   * </p>
   *
   * @param t
   * @return
   */
  Tree<T> elem(T t);

  /**
   * <p>
   * use current node as root of new tree
   * </p>
   *
   * @return
   */
  Tree<T> asNew();

  /**
   * <p>
   * return the path of current node
   * </p>
   *
   * @return
   */
  String[] path();

  /**
   * <p>
   * return the root
   * </p>
   *
   * @return
   */
  Tree<T> root();

  /**
   * <p>
   * append new node to current node
   * </p>
   *
   * @param t
   * @return
   */
  Tree<T> add(Tree<T> t);

  /**
   * <p>
   * append current node to the underlying node
   * </p>
   *
   * @param t
   * @return
   */
  Tree<T> addTo(Tree<T> t);

  /**
   * <p>
   * return the children
   * </p>
   *
   * @return
   */
  List<Tree<T>> children();

  /**
   * <p>
   * return its parent node, if not found,return null
   * </p>
   *
   * @return
   */
  Tree<T> parent();

  /**
   * <p>
   * return its parents all to the root
   * </p>
   *
   * @return
   */
  List<Tree<T>> parents();

  /**
   * <p>
   * find a node with its name using BFS
   * </p>
   *
   * @param name
   * @return
   */
  Tree<T> find(String name);

  /**
   * <p>
   * find a node with underlying function using BFS
   * </p>
   *
   * @param findFunc
   * @return
   */
  Tree<T> find(Function<Boolean, Tree<T>> findFunc);

  /**
   * <p>
   * Select from children,return the limit one satisfy the input
   * </p>
   *
   * @param name
   * @return
   */
  Tree<T> select(String name);

  /**
   * <p>
   * Recursively call select(name) using the parameter array, return the
   * latest found. If no one was satisfied, return null.
   * </p>
   *
   * @param name
   * @return
   */
  Tree<T> select(String[] name);

  /**
   * <p>
   * Last one in children.
   * </p>
   *
   * @return
   */
  Tree<T> last();

  /**
   * <p>
   * First one in children.
   * </p>
   *
   * @return
   */
  Tree<T> first();

  /**
   * <p>
   * Return children of parent
   * <p>
   *
   * @return
   */
  List<Tree<T>> siblings();

  /**
   * <p>
   * First one in children.
   * </p>
   *
   * @return
   */
  Tree<T> prev();

  /**
   * <p>
   * Return prevAll in children
   * </p>
   *
   * @return
   */
  List<Tree<T>> prevAll();

  /**
   * <p>
   * Insert before current node
   * </p>
   *
   * @param t
   * @return
   */
  Tree<T> before(Tree<T> t);

  /**
   * <p>
   * Return the next node
   * </p>
   *
   * @return
   */
  Tree<T> next();

  /**
   * <p>
   * Insert after current node
   * </p>
   *
   * @param t
   * @return
   */
  Tree<T> after(Tree<T> t);

  /**
   * <p>
   * Return nextAll of current node
   * </p>
   *
   * @return
   */
  List<Tree<T>> nextAll();

  /**
   * <p>
   * check if leaf</p>
   *
   * @return
   */
  boolean isLeaf();

  boolean isLast();

  boolean isFirst();

  boolean isRoot();

  void remove();

  Tree<T> remove(Tree<T> t);

  Tree<T> remove(Iterable<Tree<T>> t);

  Iterator<Tree<T>> bfs();

  Iterator<Tree<T>> dfs();

  Selector<Tree<T>> selector();

  String toString();
}
