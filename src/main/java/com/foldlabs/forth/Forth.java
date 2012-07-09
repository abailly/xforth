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
      f.dictionary  = new HashMap<>(a.dictionary);
      P2<Iterable<Object>, Forth> output = f.input(thread.toArray());
      return Option.some((Object)output._1());
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

  public static enum State {
    Compiling, Executing;
  }

  public static abstract class Word extends F<Forth, Option<Object>> {

    public final boolean immediate;

    public Word(boolean immediate) {
      this.immediate = immediate;
    }

  }

  private Map<Object, Word> dictionary = new HashMap<Object, Word>() {
    {
      put(DOT, new Word(false) {

        public Option<Object> f(Forth arg0) {
          return Option.some(arg0.stack.pop());
        }
      });

      put(PLUS, new Word(false) {

        public Option<Object> f(Forth arg0) {
          Stack<Object> stack = arg0.stack;
          int i = (Integer) stack.pop() + (Integer) stack.pop();
          stack.push(i);
          return Option.none();
        }
      });

      put(COLON, new Word(false) {

        public Option<Object> f(Forth forth) {
          forth.state = Forth.State.Compiling;
          forth.definingWord = forth.input.remove(0);
          forth.compilingThread = new Stack<>();
          return Option.none();
        }
      });

      put(ENTER, new Word(true) {

        public Option<Object> f(Forth forth) {
          forth.state = Forth.State.Executing;
          dictionary.put(forth.definingWord, new UserWord(false, forth.compilingThread));
          return Option.none();
        }
      });
    }
  };

  private Stack<Object> stack = new Stack<>();
  private State state = State.Executing;
  private Object definingWord = null;
  private Stack<Object> compilingThread = null;

  private ArrayList<Object> input;

  public P2<Iterable<Object>, Forth> input(Object... objects) {
    this.input = new ArrayList<>();
    this.input.addAll(Arrays.asList(objects));
    List<Object> ret = List.nil();
    while(!input.isEmpty()) {
      Object object = input.remove(0);
      if (state == State.Executing) {
        if (object instanceof Integer) {
          stack.push(object);
        } else {
          Word word = dictionary.get(object);
          if (word == null) {
            throw new ForthException("unknown word '" + object + "'");
          }
          Option<Object> output = word.f(this);
          if (output.isSome()) {
            ret = ret.cons(output.some());
          }
        }
      } else {
        Word word = dictionary.get(object);
        if (word != null && word.immediate) {
          Option<Object> output = word.f(this);
          if (output.isSome()) {
            ret = ret.cons(output.some());
          }
        } else {
          compilingThread.add(object);
        }
      }
    }
    return p((Iterable<Object>) ret, this);
  }

}
