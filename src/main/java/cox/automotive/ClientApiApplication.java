package cox.automotive;

import io.swagger.client.*;
//import io.swagger.client.auth.*;
//import io.swagger.client.model.*;
//import io.swagger.client.api.ClientApi;
import io.swagger.client.api.DataSetApi;
import io.swagger.client.api.VehiclesApi;
import io.swagger.client.api.DealersApi;

import io.swagger.client.model.Answer;
import io.swagger.client.model.AnswerResponse;
import io.swagger.client.model.VehicleAnswer;
import io.swagger.client.model.VehicleResponse;
import io.swagger.client.model.DealerAnswer;
import io.swagger.client.model.DealersResponse;

//import java.io.File;
//import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ClientApiApplication {
	
	private static DealersApi dealersApiInstance;
	private static VehiclesApi vehiclesApiInstance;

    public static void main(String[] args) {
        
        DataSetApi dataSetApiInstance = new DataSetApi();
        vehiclesApiInstance = new VehiclesApi();
        dealersApiInstance = new DealersApi();
        Answer answer = new Answer();
        try {
            String dataSetId = dataSetApiInstance.getDataSetId().getDatasetId();
            List<Integer> vehicleIds = vehiclesApiInstance.getIds(dataSetId).getVehicleIds();
            Map<Integer, List<VehicleAnswer>> dealerVehicles = new HashMap<>(); // key: dealerId
            Set<Integer> dealerIds = new HashSet<>();
            
            for(int vehicleId: vehicleIds) {
            	addVehicleAnswer(dataSetId, vehicleId, dealerIds, dealerVehicles);
            }
            
            List<DealerAnswer> dealers = getDealerAnswers(dealerIds, dataSetId, dealerVehicles);
            
            answer.dealers(dealers);
            
            AnswerResponse answerResponse = dataSetApiInstance.postAnswer(dataSetId, answer);
            System.out.println(answerResponse.toString());
            
        } catch (ApiException e) {
            System.err.println("Exception when calling ClientApi#generate");
            e.printStackTrace();
        }
    }
    
    /**
     * Get vehicleResponse from vehicle id, generate vehicleAnswer and add it to dealers list of vehicles, add dealerId to dealers Set.
     * @param dataSetId
     * @param vehicleId
     * @param dealerIds
     * @param dealerVehicles
     */
    private static void addVehicleAnswer(String dataSetId, int vehicleId, Set<Integer> dealerIds, Map<Integer, List<VehicleAnswer>> dealerVehicles) {  	
    	VehicleResponse vehicleResponse;
		try {
			vehicleResponse = vehiclesApiInstance.getVehicle(dataSetId, vehicleId);
			VehicleAnswer vehicleAnswer = new VehicleAnswer();
	    	vehicleAnswer.setVehicleId(vehicleId);
	    	vehicleAnswer.setYear(vehicleResponse.getYear());
	    	vehicleAnswer.setModel(vehicleResponse.getModel());
	    	vehicleAnswer.setMake(vehicleResponse.getMake());
	    	
	    	int dealerId = vehicleResponse.getDealerId();
	    	List<VehicleAnswer> vehicles = dealerVehicles.getOrDefault(dealerId, new ArrayList<VehicleAnswer>());
	    	vehicles.add(vehicleAnswer);
	    	
	    	dealerIds.add(dealerId);
	    	dealerVehicles.put(dealerId, vehicles);
		} catch (ApiException e) {
			System.err.println("Exception when calling VehiclesApi#getVehicle");
			e.printStackTrace();
		}
    }
    
    /**
     * Return list of DealerAnswers. Get dealersResponse from dealer id, generate dealerAnswer and add it to list of dealerAnswers.
     * @param dealerIds
     * @param dataSetId
     * @param dealerVehicles
     * @return List<DealerAnswer>
     */
    private static List<DealerAnswer> getDealerAnswers(Set<Integer> dealerIds, String dataSetId, Map<Integer, List<VehicleAnswer>> dealerVehicles) {
    	List<DealerAnswer> dealers = new ArrayList<DealerAnswer>();
        
    	for(int dealerId: dealerIds) {
        	DealersResponse dealersResponse;
			try {
				dealersResponse = dealersApiInstance.getDealer(dataSetId, dealerId);
				DealerAnswer dealerAnswer = new DealerAnswer();
	        	dealerAnswer.setDealerId(dealersResponse.getDealerId());
	        	dealerAnswer.setName(dealersResponse.getName());
	        	dealerAnswer.setVehicles(dealerVehicles.get(dealerId));
	        	dealers.add(dealerAnswer);
			} catch (ApiException e) {
				System.err.println("Exception when calling DealersApi#getDealer");
				e.printStackTrace();
			}
        }
        
        return dealers;
    }
}