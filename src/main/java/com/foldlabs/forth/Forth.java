package com.foldlabs.forth;

import static fj.P.p;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import fj.F;
import fj.P2;
import fj.data.List;
import fj.data.Option;

public class Forth {

  private static class UserWord extends Word {

    private ArrayList<Object> thread;

    public UserWord(boolean immediate, Stack<Object> compilingThread) {
      super(immediate);
      this.thread = new ArrayList<>(compilingThread);
    }

    @Override
    public Option<Object> f(Forth a) {
      Forth f = new Forth();
      f.stack = a.stack;
      f.dictionary = new HashMap<>(a.dictionary);
      P2<Iterable<Object>, Forth> output = f.input(thread.toArray());
      return Option.some((Object) output._1());
    }
  }

  public static final String DOT = ".";
  public static final String PLUS = "+";
  public static final Object COLON = ":";
  public static final Object ENTER = ";";

  public static final Object OK = new Object() {

    @Override
    public String toString() {
      return "OK";
    }

  };

  public static interface State {
    List<Object> eval(List<Object> ret, Object object);
  }

  private State compiling = new State() {

    @Override
    public List<Object> eval(List<Object> ret, Object object) {
      Word word = dictionary.get(object);
      if (word != null && word.immediate) {
        Option<Object> output = word.f(Forth.this);
        if (output.isSome()) {
          ret = ret.cons(output.some());
        }
      } else {
        compilingThread.add(object);
      }
      return ret;
    }
  };

  private State executing = new State() {

    @Override
    public List<Object> eval(List<Object> ret, Object object) {
      if (isLiteral(object)) {
        stack.push(object);
        return ret;
      }

      Word word = dictionary.get(object);
      if (word == null) {
        throw new ForthException("unknown word '" + object + "'");
      }
      Option<Object> output = word.f(Forth.this);
      if (output.isSome()) {
        ret = ret.cons(output.some());
      }
      return ret;
    }
  };

  public static abstract class Word extends F<Forth, Option<Object>> {

    public final boolean immediate;

    public Word(boolean immediate) {
      this.immediate = immediate;
    }

  }

  private Map<Object, Word> dictionary = new HashMap<Object, Word>() {
    {
      put(DOT, new Word(false) {

        public Option<Object> f(Forth forth) {
          if(forth.stack.isEmpty()) {
            throw new ForthException("empty stack: Cannot display");
          }
          return Option.some(forth.stack.pop());
        }
      });

      put(PLUS, new Word(false) {

        public Option<Object> f(Forth forth) {
          Stack<Object> stack = forth.stack;
          int i = (Integer) stack.pop() + (Integer) stack.pop();
          stack.push(i);
          return Option.none();
        }
      });

      put(COLON, new Word(false) {

        public Option<Object> f(Forth forth) {
          forth.state = compiling;
          forth.definingWord = forth.input.remove(0);
          forth.compilingThread = new Stack<>();
          return Option.none();
        }
      });

      put(ENTER, new Word(true) {

        public Option<Object> f(Forth forth) {
          forth.state = executing;
          dictionary.put(forth.definingWord, new UserWord(false, forth.compilingThread));
          return Option.none();
        }
      });
    }
  };

  private Stack<Object> stack = new Stack<>();
  private State state = executing;
  private Object definingWord = null;
  private Stack<Object> compilingThread = null;

  private ArrayList<Object> input;

  public P2<Iterable<Object>, Forth> input(Object... objects) {
    this.input = new ArrayList<>();
    this.input.addAll(Arrays.asList(objects));
    List<Object> ret = List.nil();
    while (!input.isEmpty()) {
      Object object = input.remove(0);
      ret = state.eval(ret, object);
    }
    return p((Iterable<Object>) ret, this);
  }

  boolean isLiteral(Object object) {
    return object instanceof Integer;
  }

}
