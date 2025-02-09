package me.wikimor.lpc;

import me.wikmor.lpc.LPC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnableColorTests {

  @Test
  public void shouldEnableVanillaColors() {
    makeCase(
      "&cHello, &bworld! &o:)", true, false,
      "§cHello, §bworld! §o:)"
    );
  }

  @Test
  public void shouldNotEnableVanillaColors() {
    makeCase(
      "&cHello, &bworld! &o:)", false, false,
      "&cHello, &bworld! &o:)"
    );
  }

  @Test
  public void shouldEnableHexColors() {
    makeCase(
      "&#FF0000Hello, &#00FF00world! &#0000FF:)", false, true,
      "§x§F§F§0§0§0§0Hello, §x§0§0§F§F§0§0world! §x§0§0§0§0§F§F:)"
    );
  }

  @Test
  public void shouldNotEnableHexColors() {
    makeCase(
      "&#FF0000Hello, &#00FF00world! &#0000FF:)", false, false,
      "&#FF0000Hello, &#00FF00world! &#0000FF:)"
    );
  }

  @Test
  public void shouldEnableAnyColors() {
    makeCase(
      "&#FF0000Hello, &cworld! &#0000FF:)", true, true,
      "§x§F§F§0§0§0§0Hello, §cworld! §x§0§0§0§0§F§F:)"
    );
  }

  @Test
  public void shouldNotEnableAnyColors() {
    makeCase(
      "&#FF0000Hello, &cworld! &#0000FF:)", false, false,
      "&#FF0000Hello, &cworld! &#0000FF:)"
    );
  }

  private static void makeCase(String input, boolean allowVanilla, boolean allowHex, String expectedOutput) {
    assertEquals(expectedOutput, LPC.enableColors(input, allowVanilla, allowHex));
  }
}
