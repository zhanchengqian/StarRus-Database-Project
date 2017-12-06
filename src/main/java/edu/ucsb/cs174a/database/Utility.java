package edu.ucsb.cs174a.database;

import java.io.*;
import java.util.Scanner;

/**
 * Created by zhanchengqian on 2017/12/4.
 */
public class Utility {
    public static void main(String[] args) throws IOException {
        Scanner path = new Scanner(System.in);
        if (!path.hasNext())
            System.out.println("Must enter file name!");
//        System.out.println(extract(path.next()));
        FileReader fileReader = new FileReader(path.next());
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        String query = ""; Boolean flag = false;
        while ((line = br.readLine()) != null) {
//            System.out.println(line);
            if (line.contains("(query)"))
                continue;
            if (line.contains("query") && !line.contains("="))
                continue;
            if (line.contains("query") && line.contains(";")){
                query += extract(line);
                System.out.println(query + ";");
                query = "";
                flag = false;
            }
            else if (line.contains("query") && !line.contains(";")){
                query += extract(line);
                flag = true;
            }
            else if (!line.contains("query") && flag && !line.contains(";")){
                query += extract(line);
            }
            else if (!line.contains("query") && flag && line.contains(";")){
                query += extract(line);
                System.out.println(query + ";");
                query = "";
                flag = false;
            }
        }
    }

    public static String extract (String str){
        int firstIndex = str.indexOf("\"");
        int secondIndex = str.indexOf("\"", firstIndex+1);
        return str.substring(firstIndex+1, secondIndex);
    }

}
