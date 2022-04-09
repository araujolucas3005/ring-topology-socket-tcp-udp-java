package br.ufersa.ring.utils;

public class NumberUtils {

  public static boolean isDigit(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException err) {
      return false;
    }

    return true;
  }
}
