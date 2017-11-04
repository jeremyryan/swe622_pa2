package edu.gmu.swe622.pa2;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jmr on 11/1/17.
 */
public class Test {
    public static void main(String[] args) {
        //FileSystems.getDefault().getRootDirectories().forEach(System.out::println);
        //Path path = FileSystems.getDefault().getRootDirectories().iterator().next();
        Path dot = Paths.get(".");
        Path doubleDot = Paths.get("..");
        Path path = Paths.get(".");
        Path path2 = Paths.get("..");
        System.out.println(path.equals(dot));
        System.out.println(path2.equals(doubleDot));
    }
}
