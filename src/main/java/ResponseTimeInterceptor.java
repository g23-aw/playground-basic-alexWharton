import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

@Interceptor
public class ResponseTimeInterceptor implements IClientInterceptor {
	 private StopWatch stopwatch;
	 private List<Long> responseTimes;

	 public ResponseTimeInterceptor() {
		 stopwatch = new StopWatch();
		 responseTimes = new ArrayList<Long>();
	  }
   
	 public double getAverageResponseTime() {
	   return responseTimes.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
	 }
	 
	 public void clearResponseTimes() {
		 responseTimes.clear();
	 }

	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		stopwatch.restart();
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		responseTimes.add(stopwatch.getMillis());
	}
	
	@Test
	public void testResponseTimeInterceptor() {
		ResponseTimeInterceptor responseTimeInterceptor = new ResponseTimeInterceptor();

		try {
			responseTimeInterceptor.clearResponseTimes();
			assertTrue (responseTimeInterceptor.getAverageResponseTime() == 0);

			for (int i = 1; i < 4; i++) {
				responseTimeInterceptor.interceptRequest(null);
				Thread.sleep(5000);
				responseTimeInterceptor.interceptResponse(null);
			}
			
			assertTrue (responseTimeInterceptor.getAverageResponseTime() > 5000 && 
					    responseTimeInterceptor.getAverageResponseTime() < 5025);
			
			responseTimeInterceptor.clearResponseTimes();
			assertTrue (responseTimeInterceptor.getAverageResponseTime() == 0);

			for (int i = 1; i < 4; i++) {
				responseTimeInterceptor.interceptRequest(null);
				Thread.sleep(2000 * i);
				responseTimeInterceptor.interceptResponse(null);
			}
			
			assertTrue (responseTimeInterceptor.getAverageResponseTime() > 4000 && 
				        responseTimeInterceptor.getAverageResponseTime() < 4025);
		
			responseTimeInterceptor.clearResponseTimes();
			assertTrue (responseTimeInterceptor.getAverageResponseTime() == 0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}