package com.foldlabs.forth;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

public class ForthInterpreterTest {

  @Test
  public void can_interpret_input_from_reader_and_output_to_writer() throws IOException {
    StringWriter writer = new StringWriter();
    new ForthInterpreter().interpret(new StringReader("4 3 + .\n\r2 2 + ."), writer);
    assertThat(writer.toString()).matches("7.*4");
  }
}
