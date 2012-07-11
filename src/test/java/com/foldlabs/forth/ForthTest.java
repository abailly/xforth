package com.foldlabs.forth;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import fj.P2;

public class ForthTest {

  @Test
  public void outputs_integer_when_integer_entered_then_dot() {
    Forth forth = new Forth();

    P2<Iterable<Object>, Forth> input4 = forth.input(4);
    assertThat(input4._1()).isEmpty();

    P2<Iterable<Object>, Forth> inputDot = input4._2().input(".");
    assertThat(inputDot._1()).contains(4);
  }

  @Test
  public void can_add_two_numbers() throws Exception {
    assertThat(new Forth().input(4, 3, "+", Forth.DOT)._1()).contains(7);
  }

  @Test
  public void can_output_multiple_values() throws Exception {
    assertThat(new Forth().input(4, 3, "+", Forth.DOT, 2, 2, "+", Forth.DOT)._1()).contains(7, 4);
  }

  @Test
  public void can_define_new_words() throws Exception {
    assertThat(new Forth().input(Forth.COLON, "foo", "+", Forth.ENTER, 4, 3, "foo", Forth.DOT)._1()).contains(7);
  }

  @Test
  public void can_define_new_words_containing_undefined_words() throws Exception {
    assertThat(new Forth().input(Forth.COLON, "foo", "bar", Forth.ENTER)._1()).isEmpty();
  }

  @Test
  public void comments_are_ignored() throws Exception {
    assertThat(new Forth().input(3, "(", "this", "is", "a", "comment", ")", 4, Forth.PLUS, Forth.DOT)._1()).contains(7);
  }

  @Test
  public void can_evaluate_single_branch_conditionals() throws Exception {
    assertThat(new Forth().input(0, Forth.IF, 3, Forth.DOT, Forth.THEN, 4, Forth.DOT)._1()).containsOnly(4);
    assertThat(new Forth().input(1, Forth.IF, 3, Forth.DOT, Forth.THEN, 4, Forth.DOT)._1()).containsOnly(3, 4);
  }

  @Test(expected = ForthException.class)
  public void fails_when_input_word_is_unknown() throws Exception {
    new Forth().input("foo");
  }

  @Test(expected = ForthException.class)
  public void dot_fails_when_stack_is_empty() throws Exception {
    new Forth().input(Forth.DOT);
  }

  @Test(expected = ForthException.class)
  public void colon_fails_when_it_has_no_input() throws Exception {
    new Forth().input(Forth.COLON);
  }

  @Test(expected = ForthException.class)
  public void if_fails_when_it_has_not_enough_input() throws Exception {
    new Forth().input(0,Forth.IF,3);
  }
}
