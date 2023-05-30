package com.example.miniback.Controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin("http://localhost:3000")
public class Controlleur {
    public Details details;
    public List<List<String>> data;

   public static List<List<String>> loadData(File weatherFile) {
        List<List<String>> weatherTable = new ArrayList<>();
       String delimiter = ",";
        try (BufferedReader reader = new BufferedReader(new FileReader(weatherFile))) {
            String line;
            int numInstances = 0;
            int numAttributes = 0;

           while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("@")) {
                    numInstances++;
                    String [] fields = line.split(delimiter);
                    numAttributes = Math.max(numAttributes, fields.length);
                }
            }

            String [][] weatherData = new String[numInstances][numAttributes];

           reader.close();
            BufferedReader read = new BufferedReader(new FileReader(weatherFile));

           int instanceIndex = 0;
            while ((line = read.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("@")) {
                    String [] fields = line.split(delimiter);
                    for (int attributeIndex = 0; attributeIndex < fields.length; attributeIndex++) {
                        weatherData[instanceIndex][attributeIndex] = fields[attributeIndex];
                    }
                    instanceIndex++;
                }
            }

           for (int i = 0; i < weatherData.length; i++) {
                List<String> innerList = new ArrayList<>();
                for (int j = 0; j < weatherData[i].length; j++) {
                    innerList.add(weatherData[i][j]);
                }
                weatherTable.add(innerList);
            }

            read.close();
            return weatherTable;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

   public static float calculateProbability(String input,List<List<String>> weatherTable) {
        float probability = 0;
        int appear = 0;
        int totalRows = weatherTable.size();

        HashSet<String> attributeSet = new HashSet<>();

       HashMap<String, Integer> attributeCountMap = new HashMap<>();

        for (List<String> innerList : weatherTable) {
            for (String attribute : innerList) {

               if (!attributeSet.contains(attribute)) {
                    attributeSet.add(attribute);
                    attributeCountMap.put(attribute, 0);
                }

               if (input.equals(attribute)) {
                    appear++;
                    attributeCountMap.put(attribute, attributeCountMap.get(attribute) + 1);
                }
            }
        }

        if (appear > 0) {
            probability = (float) appear / totalRows;
        }

        return probability;
    }

    public static float calculateConditionalProbability(String input, String className,List<List<String>> weatherTable) {
        int appearInClass = 0;
        int nbrClass = 0;
        float probability = 0;

        HashSet<String> attributeSet = new HashSet<>();

        for (List<String> innerList : weatherTable) {
            for (String attribute : innerList) {
                if (!attributeSet.contains(attribute)) {
                    attributeSet.add(attribute);
                }

                if (attribute.equals(input) && innerList.get(innerList.size() - 1).equals(className)) {
                    appearInClass++;
                }
            }
        }

      for (List<String> innerList : weatherTable) {
            if (innerList.get(innerList.size() - 1).equals(className)) {
                nbrClass++;
            }
        }

       for (List<String> innerList : weatherTable) {
            if (className.equals(innerList.get(innerList.size() - 1)) && nbrClass > 0) {
                probability = (float) appearInClass / nbrClass;
                break;
            }
        }
        return probability;
    }

   public static Set<String> getClasses(List<List<String>> weatherTable) {
        Set<String> className = new HashSet<>();

        for (List<String> innerList : weatherTable) {
            String attribute = innerList.get(innerList.size() - 1);
            className.add(attribute);
        }

        return className;
    }
public static float [] calculateFinalProbability(List<List<String>> weatherTable, String... attributes) {
        int i = 0;
        Set<String> className = getClasses(weatherTable);
        float [] probability = new float[className.size()];

        for (String attribute : className) {
            float attributeProbability = calculateProbability(attribute,weatherTable);
            float conditionalProbability = 1.0f;

            for (String attr : attributes) {
                conditionalProbability *= calculateConditionalProbability(attr, attribute,weatherTable);
            }

            probability[i] = attributeProbability * conditionalProbability;
            i++;
        }

        return probability;
    }

   public static String shouldPlay(List<List<String>> weatherTable,String... attributes) {

        int index = 0;
        float max = 0;
        Set<String> className = getClasses(weatherTable);

      ArrayList<String> classList = new ArrayList<>(className);

        float [] finalProbabilities = calculateFinalProbability(weatherTable,attributes);
        for (int i = 0; i < finalProbabilities.length; i++) {
            if (finalProbabilities[i] > max) {
                max = finalProbabilities[i];
                index = i;
            }
        }

       return classList.get(index);
    }
    public static SplitData devideData(List<List<String>> data){
        Collections.shuffle(data);
        int splitIndex = (int) (data.size() * 0.7);
        List<List<String>> trainingData = data.subList(0, splitIndex);
        List<List<String>> testData = data.subList(splitIndex, data.size());
        SplitData splitData = new SplitData(trainingData,testData);
        return splitData;
    }

    @PostMapping  ("/hi")
    public Prediction save(@RequestBody Data data){
        List<String> strings = data.getStrings();
        List<List<String>> weatherTable  = this.data;
        float [] table = calculateFinalProbability(weatherTable,strings.get(0),strings.get(1),strings.get(2),strings.get(3));

        for (int i = 0; i < table.length; i++) {
            System.out.print( table[i] + "\t");
        }
        System.out.println();
        String word = shouldPlay(weatherTable,strings.get(0),strings.get(1),strings.get(2),strings.get(3));

        if (word.equals("yes")) {
            System.out.println("You can play");
        } else {
            System.out.println("You can't play");
        }
         Prediction prediction = new Prediction(table[1],table[0],word );
        return prediction;
    }


    @PostMapping("/import")
    public ResponseEntity<List<List<String>>> importCSV(@RequestParam("file") MultipartFile file) {
       if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
  if (!StringUtils.endsWithIgnoreCase(file.getOriginalFilename(), ".csv")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<List<String>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               String[] values = line.split(",");
                List<String> row = new ArrayList<>();
                for (String value : values) {
                    row.add(value);
                }
                data.add(row);

            }
            this.data = data;
            SplitData splitData = devideData(data) ;
            List<List<String>> tst = splitData.testData;
            List<List<String>> train = splitData.trainData;
            System.out.println("hello");
            int countTRUS=0;
            int countFALS=0;
            for (List<String> i :tst ) {

                    if(i.get(i.size()-1).equals(shouldPlay(train,i.get(0),i.get(1),i.get(2),i.get(3)))) countTRUS++;
                    else countFALS++;


            }
            System.out.println("trus="+countTRUS);
            System.out.println("fals="+countFALS);
            details = new Details(splitData.trainData,splitData.testData,countTRUS,countFALS);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/details")
    public Details getDetails(){
        Details details1 =details;
        System.out.println("details");
        return details1;
    }
    /*
    @GetMapping("confusion")
    public Double[] confusion(){
       SplitData splitData = devideData(data);
       int
        for (List<String> i :splitData.testData ) {

            if(i.get(i.size()-1).equals(shouldPlay(splitData.trainData,i.get(0),i.get(1),i.get(2),i.get(3))) && i.get(i.size()-1).equals("yes")) countTRUS++;
            else countFALS++;


        }

    }

   */
}
