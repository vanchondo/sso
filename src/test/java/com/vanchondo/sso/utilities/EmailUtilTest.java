package com.vanchondo.sso.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
public class EmailUtilTest {

  @Test
  public void testEncode() {
    assertEquals("myEmail%40gmail.com",EmailUtil.encode("myEmail@gmail.com"));
  }

}
