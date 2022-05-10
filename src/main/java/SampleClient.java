import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class SampleClient {

    private static FhirContext fhirContext;
    private static IGenericClient client;
    private static ResponseTimeInterceptor responseTimeInterceptor;
    private static List<Patient> testListOfPatients;
    private static List<Double> testAverageResponseTimes;
    private static final boolean printPatientInfo = false;
    private static String nameFileString = "C:\\Users\\Alex\\programming\\playground-basic-alexWharton\\src\\main\\java\\LastNames.txt";
    private static boolean isRunTest = false;

    public static void main(String[] theArgs) {

        // Create a FHIR client
        fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        responseTimeInterceptor = new ResponseTimeInterceptor();
        client.registerInterceptor(responseTimeInterceptor);

        readNameFile();
    }

    private static void readNameFile() {

        boolean useCache;

        for (int i = 1; i <= 3; i++) {

            if (i == 3) {
                useCache = false;
            }
            else {
                useCache = true;
            }

            try {
                File nameFileHandler = new File(nameFileString);
                Scanner myReader = new Scanner(nameFileHandler);
                System.out.println("Running loop iteration " + i);
                while (myReader.hasNextLine()) {
                    String name = myReader.nextLine();
                    sendAndProcessQuery(name, useCache);
                }

                System.out.println("Average response time: " + responseTimeInterceptor.getAverageResponseTime() + " milliseconds. Cache was " + (useCache == true ? "used." : "not used."));

                if (isRunTest) {
                    testAverageResponseTimes.add(responseTimeInterceptor.getAverageResponseTime());
                }

                responseTimeInterceptor.clearResponseTimes();

                myReader.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendAndProcessQuery(String name, Boolean useCache) {

        List<Patient> listOfPatients = new ArrayList<Patient>();

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(name))
                .returnBundle(Bundle.class)
                .cacheControl(new CacheControlDirective().setNoCache(!useCache))
                .execute();

        for (int i = 0; i < response.getEntry().size(); i++) {
             Patient pt = (Patient) response.getEntry().get(i).getResource();

             if (printPatientInfo) {
                 System.out.println(pt.getName().get(0).getGivenAsSingleString());
                 System.out.println(pt.getName().get(0).getFamily());
                 System.out.println(pt.getBirthDate());
             }

             if (listOfPatients.size() == 0) {
                 listOfPatients.add(pt);
             } else {
                 // a binary search could be performed here for optimal speed, but I am assuming
                 // that speed is not a concern here - ordering patients by alphabetical order of first name
                 for (int index = 0; index < listOfPatients.size(); index++) {
                    if (0 <= pt.getName().get(0).getGivenAsSingleString().compareToIgnoreCase(listOfPatients.get(index).getName().get(0).getGivenAsSingleString())) {
                        listOfPatients.add(index, pt);
                        break;
                    }
                 }
             }
        }

        if (printPatientInfo) {
            // print names in alphabetical order
            for (int i = 0; i < listOfPatients.size(); i++) {
                System.out.println(listOfPatients.get(i).getName().get(0).getGivenAsSingleString());
                System.out.println(listOfPatients.get(i).getName().get(0).getFamily());
                System.out.println(listOfPatients.get(i).getBirthDate());
            }
        }

        if (isRunTest) {
            testListOfPatients.addAll(listOfPatients);
        }

     }

    @Test
    public void testSampleClient() {
        String[] args = null;
        testListOfPatients = new ArrayList<Patient>();
        testAverageResponseTimes = new ArrayList<Double>();

        isRunTest = true;
        nameFileString = "C:\\Users\\Alex\\programming\\playground-basic-alexWharton\\src\\main\\java\\testNameFile.txt";

        try {
            FileWriter fileWriter = new FileWriter(nameFileString);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print("SMITH\r\nBROWN\r\n");
            printWriter.close();
          } catch (Exception e) {
              e.printStackTrace();
          }

        SampleClient.main(args);

        for (int i = 0; i < testListOfPatients.size(); i++) {
            assertTrue(testListOfPatients.get(i).getName().get(0).getFamily().toLowerCase().contains("smith") ||
                       testListOfPatients.get(i).getName().get(0).getFamily().toLowerCase().contains("brown"));
        }

        assertTrue(testAverageResponseTimes.get(0) > testAverageResponseTimes.get(1));
        assertTrue(testAverageResponseTimes.get(2) > testAverageResponseTimes.get(1));
    }
}
