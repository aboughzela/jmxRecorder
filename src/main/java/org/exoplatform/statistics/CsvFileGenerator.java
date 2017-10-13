package org.exoplatform.statistics;


import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CsvFileGenerator {


    //Default separator
    char SEPARATOR = ',';

    FileWriter fileWriter;

    String pathFile;


    public CsvFileGenerator(String pathFile) throws IOException {
        this.pathFile = pathFile;

        this.fileWriter = new FileWriter(this.pathFile, true);
    }

    /**
     * function write line in csv
     * @param line
     * @throws IOException
     */
    public void writeLine(Collection<Object> line) throws IOException {
        boolean firstParam = true;
        StringBuilder stringBuilder = new StringBuilder();
        if(line != null)
        {
            for (Object param : line) {
                //if the first param in the line, separator is not needed
                if (!firstParam) {
                    stringBuilder.append(SEPARATOR);
                }
                //Add param to line
                stringBuilder.append(param);
                firstParam = false;
            }
            line.clear();
        }

        //prepare file to next line
        stringBuilder.append("\n");
        //add to file the line
        fileWriter.append(stringBuilder.toString());


    }



    public List<String[]> readFile() {
       BufferedReader bufferedReader = null;
        List<String[]> cachesConfig = new ArrayList<String[]>();
        String line = "";
        try {
            bufferedReader = new BufferedReader(new FileReader(pathFile));
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] items = line.split(String.valueOf(SEPARATOR));
                cachesConfig.add(items);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cachesConfig;
    }

    public void flush() throws IOException {
        //proper close to file
        fileWriter.flush();
        fileWriter.close();
    }
}
