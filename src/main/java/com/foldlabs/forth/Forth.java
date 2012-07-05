package com.foldlabs.forth;

import static fj.P.p;

import java.util.Stack;

import fj.P2;

public class Forth {

  private static final String DOT = ".";

  public static final Object OK = new Object() {

    @Override
    public String toString() {
      return "OK";
    }

  };

  private Stack<Object> stack = new Stack<Object>();

  public P2<Object, Forth> input(Object object) {
    if (object.equals(DOT)) {
      return p(stack.pop(), this);
    }

    stack.push(object);
    return p(OK, this);
  }

}
