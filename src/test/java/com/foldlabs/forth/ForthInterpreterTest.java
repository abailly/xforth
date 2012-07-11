package com.foldlabs.forth;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ForthInterpreterTest {

  private static final File basedir = new File(ForthInterpreter.class.getProtectionDomain().getCodeSource().getLocation().getFile());
  private final String testCase;

  @Parameters
  public static List<Object[]> testCases() throws IOException {
    List<Object[]> cases = new ArrayList<>();
    for (File testCase : new File(basedir, "test-cases").listFiles()) {
      cases.add(new Object[] { FileUtils.readFileToString(testCase) });
    }
    return cases;
  }

  public ForthInterpreterTest(String testCase) {
    this.testCase = testCase;
  }
  
  @Test
  public void run_test_case() throws IOException {
    StringWriter writer = new StringWriter();
    new ForthInterpreter().interpret(new StringReader(testCase), writer);
    assertThat(writer.toString()).matches("7.*4");
  }

}
