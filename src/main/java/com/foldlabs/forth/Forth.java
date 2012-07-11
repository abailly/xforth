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

  public static final String DOT             = ".";
  public static final String PLUS            = "+";
  public static final Object COLON           = ":";
  public static final Object ENTER           = ";";
  public static final Object START_COMMENT   = "(";
  public static final Object END_COMMENT     = ")";
  public static final Object THEN            = "THEN";
  public static final Object IF              = "IF";

  private Stack<Object>      stack           = new Stack<>();
  private Object             definingWord    = null;
  private Stack<Object>      compilingThread = null;

  private ArrayList<Object>  input;

  public static final Object OK              = new Object() {

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

                              Word word = checkWordExists(object);
                              Option<Object> output = word.f(Forth.this);
                              if (output.isSome()) {
                                ret = ret.cons(output.some());
                              }
                              return ret;
                            }
                          };

  private State state     = executing;

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
                                               return Option.some(forth.pop());
                                             }
                                           });

                                           put(PLUS, new Word(false) {

                                             public Option<Object> f(Forth forth) {
                                               forth.push((Integer) forth.pop() + (Integer) forth.pop());
                                               return Option.none();
                                             }
                                           });

                                           put(COLON, new Word(false) {

                                             public Option<Object> f(Forth forth) {
                                               forth.state = compiling;
                                               forth.definingWord = forth.input();
                                               forth.compilingThread = new Stack<>();
                                               return Option.none();
                                             }
                                           });

                                           put(ENTER, new Word(true) {

                                             public Option<Object> f(Forth forth) {
                                               defineWord();
                                               forth.state = executing;
                                               return Option.none();
                                             }
                                           });

                                           put(IF, new Word(true) {

                                             public Option<Object> f(Forth forth) {
                                               if (!forth.condition()) {
                                                 while (!THEN.equals(forth.input()))
                                                   ;
                                               }
                                               return Option.none();
                                             }
                                           });

                                           put(THEN, new Word(true) {

                                             public Option<Object> f(Forth forth) {
                                               return Option.none();
                                             }
                                           });

                                           put(START_COMMENT, new Word(true) {

                                             public Option<Object> f(Forth forth) {
                                               Object object = null;
                                               do {
                                                 object = input();
                                               } while (!END_COMMENT.equals(object));
                                               return Option.none();
                                             }
                                           });
                                         }
                                       };

  public P2<Iterable<Object>, Forth> input(Object... objects) {
    this.input = new ArrayList<>();
    this.input.addAll(Arrays.asList(objects));
    List<Object> ret = List.nil();
    while (!input.isEmpty()) {
      ret = state.eval(ret, input());
    }
    return p((Iterable<Object>) ret, this);
  }

  protected void push(Object item) {
    stack.push(item);
  }

  private Object pop() {
    if (stack.isEmpty()) {
      throw new ForthException("empty stack: Cannot display");
    }
    return stack.pop();
  }

  private Object input() {
    if (input == null || input.isEmpty()) {
      throw new ForthException("no more input");
    }
    return input.remove(0);
  }

  private boolean isLiteral(Object object) {
    return object instanceof Integer;
  }

  private Word checkWordExists(Object object) {
    Word word = dictionary.get(object);
    if (word == null) {
      throw new ForthException("unknown word '" + object + "'");
    }
    return word;
  }

  private boolean condition() {
    return 0 != (Integer) stack.pop();
  }

  private void defineWord() {
    dictionary.put(definingWord, new UserWord(false, compilingThread));
  }

}
