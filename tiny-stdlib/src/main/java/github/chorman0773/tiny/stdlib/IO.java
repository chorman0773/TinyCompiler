package github.chorman0773.tiny.stdlib;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class IO {
    static final Map<String,OutputStream> outputs = new HashMap<>();

    static OutputStream openWritePath(String name){
        if(outputs.containsKey(name))
            return outputs.get(name);
        else{
            try{
                OutputStream out = new FileOutputStream(name);
                outputs.put(name,out);
                return out;
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }

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

    static void write(String path, double d) {
        try(PrintStream print = new PrintStream(openWritePath(path))){
            print.print(d);
        }
    }

    static void write(String path, int i) {
        try(PrintStream print = new PrintStream(openWritePath(path))){
            print.print(i);
        }
    }

    static void write(String path, String s) {
        try(PrintStream print = new PrintStream(openWritePath(path))){
            print.print(s);
        }
    }
}
