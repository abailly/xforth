package com.foldlabs.forth;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FilenameFilter;
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

  private static final File   basedir        = new File(ForthInterpreterTest.class.getProtectionDomain().getCodeSource().getLocation()
                                                 .getFile());

  private static List<String> expectedOutput = new ArrayList<>();

  private static FilenameFilter testCaseFiles = new FilenameFilter() {
    
    @Override
    public boolean accept(File dir, String name) {
      return name.matches("\\d+-.*\\.f");
    }
  };

  static {
    try {
      expectedOutput = FileUtils.readLines(new File(basedir, "test-cases/expected-output.csv"));
    } catch (IOException e) {
      throw new RuntimeException("error while trying to read 'expected-output.csv'", e);
    }
  }

  private final String        testCase;
  private final int           testId;

  @Parameters
  public static List<Object[]> testCases() throws IOException {
    List<Object[]> cases = new ArrayList<>();
    for (File testCase : new File(basedir, "test-cases").listFiles(testCaseFiles )) {
      String content = FileUtils.readFileToString(testCase);
      int testId = Integer.parseInt(testCase.getName().split("-")[0]);
      cases.add(new Object[] { testId, content });
    }
    return cases;
  }

  public ForthInterpreterTest(int testId, String testCase) {
    this.testId = testId;
    this.testCase = testCase;
  }

  @Test
  public void run_test_case() throws IOException {
    StringWriter writer = new StringWriter();
    new ForthInterpreter().interpret(new StringReader(testCase), writer);
    assertThat(writer.toString()).matches(expectedOutput.get(testId - 1));
  }

}
