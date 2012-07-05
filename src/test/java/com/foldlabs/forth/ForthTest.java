package com.foldlabs.forth;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import fj.P2;

public class ForthTest {

  @Test
  public void outputs_integer_when_integer_entered_then_dot() {
    Forth forth = new Forth();

    P2<? extends Object, Forth> input4 = forth.input(4);
    assertThat(input4._1()).isEqualTo(Forth.OK);

    P2<? extends Object, Forth> inputDot = input4._2().input(".");
    assertThat(inputDot._1()).isEqualTo(4);
  }

  @Test
  public void can_add_two_numbers() throws Exception {
    assertThat(new Forth().input(4, 3, "+", Forth.DOT)._1()).isEqualTo(7);
  }

}
