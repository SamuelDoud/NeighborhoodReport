package com.RealEstate.NeighborhoodReport;

/**
 * @author Samuel Doud
 * 11/7/2015
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
    	String[] results = getBestPlaces();
    	for (int i = 0; i < results.length; i++)
    	{
    		//System.out.println(groceryStoresNearHome[i]);
    		System.out.println(results[i]);
    	}
  
    }
    /**
     * This is a user method. Asks user for a starting address and destinations
     * Calls the getAddressMethod and Travel Times on each destination
     * Finds the particular establishment of a type with the fastest travel time and
     * adds that to a string array.
     * @return results An array that holds an establishment of each type with the fastest travel time 
     */
    public static String[] getBestPlaces()
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
    	String[][] establishmentNearAddress;
    	String[] results = new String[destinationTypes.size()];
    	establishmentNearAddress = (TravelTime(baseLocation, getAddress(baseLocation, destinationTypes.toArray(new String[destinationTypes.size()]))));//Element zero is the establishment of that type that is the fastest to get to
		
		
    	for (int index = 0; index < establishmentNearAddress.length; index++)
    	{
    		results[index] = establishmentNearAddress[index][0];
    	}
    	return results;
    }
    /**
     * returns the travel times of a jagged array of destinations in a sorted manner by duration
     * 
     * @param startAddress the base address
     * @param destinations the places under examination
     * @return
     */
    public static String[][] TravelTime(String startAddress, String[][] destinations)
    {
    	String[] startAddressArr = new String[1];
    	startAddressArr[0] = startAddress;
    	String[][] fastest = new String[destinations.length][];
    	for (int destinationIndex = 0; destinationIndex < destinations.length; destinationIndex++)
    	{
	    	try
	    	{
	    		DistanceMatrix distances = DistanceMatrixApi.getDistanceMatrix(context, startAddressArr, destinations[destinationIndex]).await();
	    		//TODO sort the array by fastest travel time
	    		distances = SortByFastest(distances);//call a method to sort this matrix
	    		fastest[destinationIndex] = new String[destinations[destinationIndex].length];
	    		for (int i = 0; i < destinations[destinationIndex].length; i++)
	    		{
	    			fastest[destinationIndex][i] = destinations[destinationIndex][0] + " is " + distances.rows[0].elements[i].duration + " away.";
	    		}
	    	}
	    	catch (Exception e)
	    	{
	    		e.getStackTrace();
	    	}
    	}
    	return fastest;
    }
    /**
     * A selection sort based on the fastest travel time
     * @param matrix
     * @return
     */
    public static DistanceMatrix SortByFastest(DistanceMatrix matrix)
    {
    	//sorting algorithm
    	long minTime;
    	int index;
    	long[] travelTimes = new long[matrix.destinationAddresses.length];
    	for (int currentRow = 0; currentRow < matrix.rows.length; currentRow++)//loop through each row
    	{
			for (int i = 0; i < matrix.destinationAddresses.length; i++)//put all the travel times of the row into a long array
			{
				travelTimes[i] = matrix.rows[currentRow].elements[i].duration.inSeconds;//add a time in seconds to the travelTime array
			}
			for (int first = 0; first < travelTimes.length; first++)//go through each element in the array
			{
				index = travelTimes.length - 1;
				minTime = travelTimes[index];//this always has to be an unsorted member. Therefore this is a safe number to use as a base
				for (int second = first; second < travelTimes.length; second++)//I forget the name of this sort.. probably selection...
				{//take the least unsorted element and place it in the first index
					if (travelTimes[second] < minTime)//is the travel time of this element less than the other explored unsorted elements
					{
						minTime = travelTimes[second];//minTime and index now reflect this location's attributes
						index = second;
					}
				}
				//swap first and second
				DistanceMatrixElement temp = matrix.rows[currentRow].elements[first];
				matrix.rows[currentRow].elements[first] = matrix.rows[currentRow].elements[index];
				matrix.rows[currentRow].elements[index] = temp;
				long l = travelTimes[index];
				travelTimes[index] = travelTimes[first];
				travelTimes[first] = l;
			}
    	}
    	return matrix;
    }
    /**
     * Method takes a base address and establishments and return the addresses
     * @param baseLocation
     * @param establishmentType
     * @return
     */
    public static String[][] getAddress(String baseLocation, String[] establishmentType)
    {
    	PlacesSearchResponse responses;
    	String[][] allResults = new String[establishmentType.length][];
    	for (int type = 0; type < establishmentType.length; type++)
    	{
	    	try
	    	{
	    		responses = PlacesApi.textSearchQuery(context, establishmentType[type] + " near " + baseLocation).await();
	    		allResults[type] = new String[responses.results.length];
	    		for (int i = 0; i < responses.results.length; i++)
	    		{
		    		PlacesSearchResult result = responses.results[i];
		    		allResults[type][i] = (result.name + " " + result.formattedAddress);
	    		}
	    	}
	    	catch (Exception e)
	    	{
	    		e.printStackTrace();
	    	}
    	}
    	return allResults;
    }
}
