package com.chenkuojun.mytomcat.connector.niohttp;

import lombok.extern.slf4j.Slf4j;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A subclass of PrintWriter that automatically flushes each time
 * a print() or println() method is called.
 */

@Slf4j
public class NioResponseWriter extends PrintWriter {

  protected NioResponseStream ob;

  public NioResponseWriter(OutputStreamWriter writer, NioResponseStream ob) {
    super(writer);
    this.ob = ob;
  }

  public void print(boolean b) {
    super.print(b);
  }

  public void print(char c) {
    super.print(c);
  }

  public void print(char ca[]) {
    super.print(ca);
  }

  public void print(double d) {
    super.print(d);
  }

  public void print(float f) {
    super.print(f);
  }

  public void print(int i) {
    super.print(i);
  }

  public void print(long l) {
    super.print(l);
  }

  public void print(Object o) {
    super.print(o);
  }

  public void print(String s) {
    super.print(s);
  }

  public void println() {
    super.println();
  }

  public void println(boolean b) {
    super.println(b);
  }

  public void println(char c) {
    super.println(c);
  }

  public void println(char ca[]) {
    super.println(ca);
  }

  public void println(double d) {
    super.println(d);
  }

  public void println(float f) {
    super.println(f);
  }

  public void println(int i) {
    super.println(i);
  }

  public void println(long l) {
    super.println(l);
  }

  public void println(Object o) {
    super.println(o);
  }

  public void println(String s) {
    super.println(s);
  }

  public void write(char c) {
    super.write(c);
  }

  public void write(char ca[]) {
    super.write(ca);
  }

  public void write(char ca[], int off, int len) {
    super.write(ca, off, len);
    //super.flush();
  }

  public void write(String s) {
    super.write(s);
  }

  public void write(String s, int off, int len) {
    super.write(s, off, len);
    //super.flush();
  }
}
