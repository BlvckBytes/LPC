package me.wikimor.lpc;

import me.wikmor.lpc.LPC;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceVariablesTests {

  private static final Map<String, String> VARIABLE_MAP = Map.of(
    "variable_1", "value one",
    "variable_2", "value two",
    "variable_3", "value three"
  );

  @Test
  public void shouldPassThroughStringsDevoidOfVariables() {
    makeCase(
      "This string does not contain any variables",
      "This string does not contain any variables"
    );
  }

  @Test
  public void shouldReplaceVariableOnlyContent() {
    makeCase(
      "{variable_1}",
      "value one"
    );

    makeCase(
      "{unknown}",
      "{unknown}"
    );
  }

  @Test
  public void shouldReplaceVariablesAtTheVeryBeginning() {
    makeCase(
      "{variable_1} continued text",
      "value one continued text"
    );

    makeCase(
      "{variable_1}continued text",
      "value onecontinued text"
    );

    makeCase(
      "{unknown} continued text",
      "{unknown} continued text"
    );

    makeCase(
      "{unknown}continued text",
      "{unknown}continued text"
    );
  }

  @Test
  public void shouldReplaceVariablesAtTheVeryEnd() {
    makeCase(
      "prior text {variable_1}",
      "prior text value one"
    );

    makeCase(
      "prior text{variable_1}",
      "prior textvalue one"
    );

    makeCase(
      "prior text {unknown}",
      "prior text {unknown}"
    );

    makeCase(
      "prior text{unknown}",
      "prior text{unknown}"
    );
  }

  @Test
  public void shouldReplaceMultipleVariables() {
    makeCase(
      "prior text {variable_1} mid text {variable_2} end text",
      "prior text value one mid text value two end text"
    );

    makeCase(
      "prior text{variable_1} mid text{variable_2} end text{variable_3}",
      "prior textvalue one mid textvalue two end textvalue three"
    );
  }

  private static void makeCase(String input, String expectedOutput) {
    assertEquals(expectedOutput, LPC.replaceVariables(input, VARIABLE_MAP::get));
  }
}
