package com.foldlabs.forth;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

import fj.P2;
import fj.data.Option;
import fj.function.Integers;

public class ForthInterpreter {

  private static final int MAX_WORD_LENGTH = 1024;
  private Forth forth = new Forth();

  public ForthInterpreter interpret(Reader reader, Writer writer) throws IOException {
    int c = -1;
    CharBuffer chars = CharBuffer.allocate(MAX_WORD_LENGTH);
    while ((c = reader.read()) > -1) {
      if (Character.isWhitespace(c)) {
        if (hasSomeChars(chars)) {
          forth = interpretWord(writer, toWord(chars), forth);
        }
      } else {
        chars.put((char) c);
      }
    }
    if (hasSomeChars(chars)) {
      forth = interpretWord(writer, toWord(chars), forth);
    }
    return this;
  }

  boolean hasSomeChars(CharBuffer chars) {
    return chars.position() > 0;
  }

  private String toWord(CharBuffer chars) {
    chars.flip();
    String word = chars.toString();
    chars.clear();
    return word;
  }

  private Forth interpretWord(Writer writer, String word, Forth forth) throws IOException {
    Object input = analyze(word);
    P2<Iterable<Object>, Forth> result = forth.input(input);
    for (Object output : result._1()) {
      writer.append(output.toString());
    }
    return result._2();
  }

  private Object analyze(String word) {
    Option<Integer> maybeInt = Integers.fromString().f(word);
    if (maybeInt.isSome()) {
      return maybeInt.some();
    }
    return word;
  }
}
