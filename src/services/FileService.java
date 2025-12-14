package services;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileService
 * Simple helper for reading/writing files.
 * (Auth log is handled in LoginService.)
 */
public class FileService {

    // basic text write (append or overwrite)
    public boolean writeLine(String filename, String line, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, append))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("File write error: " + e.getMessage());
            return false;
        }
    }

    // read all lines from a text file
    public List<String> readAllLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return lines;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                lines.add(s);
            }
        } catch (IOException e) {
            System.out.println("File read error: " + e.getMessage());
        }
        return lines;
    }

    // save any object to a binary file
    public boolean saveObject(String filename, Object obj) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
            return true;
        } catch (IOException e) {
            System.out.println("Binary save error: " + e.getMessage());
            return false;
        }
    }

    // load any object from a binary file
    public Object loadObject(String filename) {
        File file = new File(filename);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        } catch (Exception e) {
            System.out.println("Binary load error: " + e.getMessage());
            return null;
        }
    }

    public boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public boolean deleteFile(String filename) {
        File file = new File(filename);
        return file.exists() && file.delete();
    }
}
