import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        List<Patient> ListOfPatients = new ArrayList<Patient>();

        for (int i = 0; i < response.getEntry().size(); i++) {
             Patient pt = (Patient) response.getEntry().get(i).getResource();
             System.out.println(pt.getName().get(0).getGivenAsSingleString());
             System.out.println(pt.getName().get(0).getFamily());
             System.out.println(pt.getBirthDate());

             if (ListOfPatients.size() == 0) {
                 ListOfPatients.add(pt);
             } else {
                 /* a binary search could be performed here for optimal speed, but
                  * I am assuming that speed is not a concern here.
                  */
                 for (int index = 0; index < ListOfPatients.size(); index++) {
                    if (0 <= pt.getName().get(0).getGivenAsSingleString().compareToIgnoreCase(ListOfPatients.get(index).getName().get(0).getGivenAsSingleString())) {
                            ListOfPatients.add(index, pt);
                        break;
                    }
                 }
             }
        }

        System.out.println("Print again, but this time in alphabetical order");

        for (int i = 0; i < ListOfPatients.size(); i++) {
            System.out.println(ListOfPatients.get(i).getName().get(0).getGivenAsSingleString());
            System.out.println(ListOfPatients.get(i).getName().get(0).getFamily());
            System.out.println(ListOfPatients.get(i).getBirthDate());
        }
    }
}
