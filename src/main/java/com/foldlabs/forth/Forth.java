package com.foldlabs.forth;

import static fj.P.p;

import java.util.Stack;

import fj.P2;
import fj.data.List;

public class Forth {

  public static final String DOT = ".";
  public static final String PLUS = "+";

  public static final Object OK = new Object() {

    @Override
    public String toString() {
      return "OK";
    }

  };

  private Stack<Object> stack = new Stack<Object>();

  public P2<Iterable<Object>, Forth> input(Object... objects) {
    List<Object> ret = List.nil();
    for (Object object : objects) {
      if (object.equals(DOT)) {
        ret = ret.cons(stack.pop());
      } else if (object.equals(PLUS)) {
        int i = (Integer) stack.pop() + (Integer) stack.pop();
        stack.push(i);
      } else stack.push(object);
    }
    return p((Iterable<Object>) ret, this);
  }

}
