package com.foldlabs.forth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import fj.P2;
import fj.data.Option;
import fj.function.Integers;

public class ForthInterpreter {

  private Forth forth = new Forth();

  public static void main(String[] args) throws IOException {
    ForthInterpreter interpreter = new ForthInterpreter();
    interpreter.interpret(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
  }

  public ForthInterpreter interpret(Reader reader, Writer writer) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line = null;
    while ((line = bufferedReader.readLine()) != null) {
      String[] inputs = line.split("\\s+");
      forth = interpretWord(writer, forth, inputs);
    }
    return this;
  }

  private Forth interpretWord(Writer writer, Forth forth, String... words) throws IOException {
    Object[] input = analyze(words);
    P2<Iterable<Object>, Forth> result = forth.input(input);
    for (Object output : result._1()) {
      writer.append(output.toString());
    }
    writer.flush();
    return result._2();
  }

  private Object[] analyze(String[] words) {
    List<Object> inputs = new ArrayList<>();
    for (int i = 0; i < words.length; i++) {
      Option<Integer> maybeInt = Integers.fromString().f(words[i]);
      if (maybeInt.isSome()) {
        inputs.add(maybeInt.some());
      } else if (!"".equals(words[i])) {
        inputs.add(words[i]);
      }
    }
    return inputs.toArray();
  }
}
