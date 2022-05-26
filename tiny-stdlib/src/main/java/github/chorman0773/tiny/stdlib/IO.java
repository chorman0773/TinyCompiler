package github.chorman0773.tiny.stdlib;

import java.io.*;
import java.util.Scanner;

class IO {
    static double readDouble(String path) throws IOException {
        try(Scanner reader = new Scanner(new FileReader(path))){
            return reader.nextDouble();
        }
    }

    static int readInt(String path) throws IOException{
        try(Scanner reader = new Scanner(new FileReader(path))){
            return reader.nextInt();
        }
    }

    static String readString(String path) throws IOException{
        try(Scanner reader = new Scanner(new FileReader(path))){
            return reader.nextLine();
        }
    }

    static void write(String path, double d) throws FileNotFoundException {
        try(PrintStream print = new PrintStream(path)){
            print.println(d);
        }
    }

    static void write(String path, int i) throws FileNotFoundException {
        try(PrintStream print = new PrintStream(path)){
            print.println(i);
        }
    }

    static void write(String path, String s) throws FileNotFoundException {
        try(PrintStream print = new PrintStream(path)){
            print.println(s);
        }
    }
}
