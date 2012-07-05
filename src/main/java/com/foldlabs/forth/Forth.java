package com.foldlabs.forth;

import static fj.P.p;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import fj.F;
import fj.P2;
import fj.data.List;
import fj.data.Option;

public class Forth {

  public static final String DOT = ".";
  public static final String PLUS = "+";

  public static final Object OK = new Object() {

    @Override
    public String toString() {
      return "OK";
    }

  };

  private static final Map<Object, F<Forth, Option<Object>>> dictionary = new HashMap<Object, F<Forth, Option<Object>>>() {
    {
      put(DOT, new F<Forth, Option<Object>>() {

        public Option<Object> f(Forth arg0) {
          return Option.some(arg0.stack.pop());
        }
      });
      
      put(PLUS, new F<Forth, Option<Object>>() {

        public Option<Object> f(Forth arg0) {
          Stack<Object> stack = arg0.stack;
          int i = (Integer) stack.pop() + (Integer) stack.pop();
          stack.push(i);
          return Option.none();
        }
      });
    }
  };

  private Stack<Object> stack = new Stack<Object>();

  public P2<Iterable<Object>, Forth> input(Object... objects) {
    List<Object> ret = List.nil();
    for (Object object : objects) {
      if (object instanceof Integer) {
        stack.push(object);
      } else {
        F<Forth, Option<Object>> word = dictionary.get(object);
        Option<Object> output = word.f(this);
        if (output.isSome()) {
          ret = ret.cons(output.some());
        }
      }
    }
    return p((Iterable<Object>) ret, this);
  }

}
