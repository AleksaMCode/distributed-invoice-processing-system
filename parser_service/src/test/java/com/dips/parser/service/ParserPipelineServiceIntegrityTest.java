package com.dips.parser.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dips.parser.util.Sha256Utils;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ParserPipelineServiceIntegrityTest {

  @Test
  void isIntegrityValidReturnsTrueForMatchingHash() throws Exception {
    ParserPipelineService service = new ParserPipelineService(null, null, null, null);
    Method method =
        ParserPipelineService.class.getDeclaredMethod(
            "isIntegrityValid", byte[].class, byte[].class);
    method.setAccessible(true);

    byte[] payload = "<invoice><id>INV-1</id></invoice>".getBytes(StandardCharsets.UTF_8);
    byte[] hash = Sha256Utils.hash(payload);

    boolean result = (boolean) method.invoke(service, payload, hash);

    assertTrue(result);
  }

  @Test
  void isIntegrityValidReturnsFalseForMismatchedHash() throws Exception {
    ParserPipelineService service = new ParserPipelineService(null, null, null, null);
    Method method =
        ParserPipelineService.class.getDeclaredMethod(
            "isIntegrityValid", byte[].class, byte[].class);
    method.setAccessible(true);

    byte[] payload = "<invoice><id>INV-1</id></invoice>".getBytes(StandardCharsets.UTF_8);
    byte[] wrongHash = new byte[32];

    boolean result = (boolean) method.invoke(service, payload, wrongHash);

    assertFalse(result);
  }
}
