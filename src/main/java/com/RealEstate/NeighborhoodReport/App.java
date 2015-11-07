package com.RealEstate.NeighborhoodReport;

/**
 * Hello world!
 *
 */
import com.google.maps.*;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.TravelMode;
import java.util.Scanner;
import java.util.ArrayList;
public class App 
{
	static GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCUnVmzye4hwjNRU3bxsNTur-fT7xnWEH8");
    public static void main( String[] args )
    {
    	String baseLocation;
    	ArrayList<String> destinationTypes = new ArrayList<String>();
    	Scanner input = new Scanner(System.in);
    	String userString;
    	System.out.println("Enter a address as a base");
    	baseLocation = input.nextLine();
    	System.out.println("Enter the places you want to go to. Press enter with no text to generate results");
    	do
    	{
    		userString = input.nextLine();
    		if (!userString.isEmpty())
    		{
    			destinationTypes.add(userString);
    		}
    	}while (!userString.isEmpty());
    	input.close();
    	String[] establishmentNearAddress;
    	String[] results = new String[destinationTypes.size()];
    	for (int index = 0; index < destinationTypes.size(); index++)
    	{
    		establishmentNearAddress = getAddress(baseLocation, destinationTypes.get(index));
    		results[index] = (TravelTime(baseLocation, establishmentNearAddress))[0];//Element zero is the establishment of that type that is the fastest to get to
    	}
    	for (int i = 0; i < results.length; i++)
    	{
    		//System.out.println(groceryStoresNearHome[i]);
    		System.out.println(results[i]);
    	}
    	
    	
    	
    	
    	
    }
    public static String[] TravelTime(String startAddress, String[] destinations)
    {
    	String[] startAddressArr = new String[1];
    	startAddressArr[0] = startAddress;
    	String[] fastest = new String[destinations.length];
    	try
    	{
    		DistanceMatrix distances = DistanceMatrixApi.getDistanceMatrix(context, startAddressArr, destinations).await();
    		//TODO sort the array by fastest travel time
    		distances = SortByFastest(distances);
    		for (int i = 0; i < fastest.length; i++)
    		{
    			fastest[i] = destinations[i] + " is " + distances.rows[0].elements[i].duration + " away.";
    		}
    	}
    	catch (Exception e)
    	{
    		e.getStackTrace();
    	}
    	return fastest;
    }
    public static DistanceMatrix SortByFastest(DistanceMatrix matrix)
    {
    	//sorting algorithm
    	long minTime;
    	int index;
    	long[] travelTimes = new long[matrix.destinationAddresses.length];
    	for (int i = 0; i < matrix.destinationAddresses.length; i++)
    	{
    		travelTimes[i] = matrix.rows[0].elements[i].duration.inSeconds;
    	}
    	for (int first = 0; first < travelTimes.length; first++)
    	{
    		index = travelTimes.length - 1;
    		minTime = travelTimes[index];
    		for (int second = first; second < travelTimes.length; second++)
    		{
    			if (travelTimes[second] < minTime)
    			{
    				minTime = travelTimes[second];
    				index = second;
    			}
    		}
    		//swap first and second
    		DistanceMatrixElement temp = matrix.rows[0].elements[first];
    		matrix.rows[0].elements[first] = matrix.rows[0].elements[index];
    		matrix.rows[0].elements[index] = temp;
    		long l = travelTimes[index];
    		travelTimes[index] = travelTimes[first];
    		travelTimes[first] = l;
    	}
    	return matrix;
    	
    }
    public static String[] getAddress(String baseLocation, String establishmentType)
    {
    	PlacesSearchResponse responses;
    	String[] results = new String[1];
    	try
    	{
    		responses = PlacesApi.textSearchQuery(context, establishmentType + " near " + baseLocation).await();
    		results = new String[responses.results.length];
    		for (int i = 0; i < responses.results.length; i++)
    		{
	    		PlacesSearchResult result = responses.results[i];
	    		results[i] = (result.name + " " + result.formattedAddress);
    		}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	return results;
    }
}
