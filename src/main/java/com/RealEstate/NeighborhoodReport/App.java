package com.RealEstate.NeighborhoodReport;

/**
 * @author Samuel Doud
 * 11/10/2015
 */
import com.google.maps.*;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
public class App 
{
	private static String APIKey = "AIzaSyCUnVmzye4hwjNRU3bxsNTur-fT7xnWEH8";
	private static GeoApiContext context = new GeoApiContext().setApiKey(APIKey);//create the object that holds my API key
    public static void main( String[] args )
    {
    	if (args.length < 2)
    	{//two arguments are needed for this function to work! A base address and at lease one destination!
    		System.out.println("Entry form is <base address> <desired locations>");
    		System.exit(-1);
    	}
    	
    	String home = args[0];
    	String[] destinations = new String[args.length - 1];
    	String filename  = home + DateTime.now().getDayOfYear() + DateTime.now().getMonthOfYear()+ DateTime.now().getDayOfMonth() + DateTime.now().getSecondOfDay();//a file name based on the time of the day
    	for (int i = 1; i < args.length; i++)
    	{
    		destinations[i - 1] = args[i];
    	}
    	String[][] writeToFile = getPlacesOrdered(home,destinations);
    	String[] printMe = GetBestPlaces(writeToFile);
    	for (int i = 0; i < printMe.length; i++)
    	{
    		System.out.println(printMe[i]);
    	}
    	System.exit(WriteToFile(filename, destinations, writeToFile));//Write to file returns a status related to its successful completion
    }
    /**
     * This method takes a filename, an array of destinations, and the time ordered list of those destinations
     * and writes it to a CSV file
     * @param filename the name of the file
     * @param input the data to be written to the file
     * @return 0 if successful, -1 if failed
     */
    public static int WriteToFile(String filename,String[] destinations, String[][] input)
    {
    	String delimiter = ",";
    	BufferedWriter output = null;
        try {
            File file = new File(filename + ".csv");//create the file
            output = new BufferedWriter(new FileWriter(file));//open a buffer

			for (int i = 0; i < input.length; i++)//go through each of the arrays on the jagged array
			{//creating the header
				output.write(Sanitize(destinations[i]) + delimiter);//write the type of destination
			}
			output.newLine();//goto the nextline
			// find the maximum number of places of a type
			int max = 0;
			for (int type = 0; type < input.length; type++)//go through each type
			{
				if (input[type].length > max)
				{
					max = input[type].length;
				}
			}
			for (int place = 0; place < max; place++)
			{//writing the data
				for (int type = 0; type < input.length; type++)
				{
					if(input[type].length > place)
					{//the location is valid
						output.write(Sanitize(input[type][place]) +delimiter);//writing to a csv, but address typically have commas in them.. must be cleaned
					}
					else
					{//the block is out of bounds, write nothing besides the delimiter
						output.write(delimiter);
					}
				}
				output.newLine();//goto the nextline
			}
			
	    	output.close();
	    	return 0;
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
    }
    /**
     * Makes the string readable in a csv file by enclosing any commas with quotation marks
     * @param s the string to be cleaned
     * @return a sanitized string
     */
    public static String Sanitize(String s)
    {
    	String sanitizedS = "\"" + s + "\"";
    	return sanitizedS;
    }
    /**
     * This method takes an address and some destinations and finds the travel time to those destinations
     * Sorts them by travel time
     * @param baseAddress the starting address for all travels
     * @param destinations the places which will be traveled to
     * @return an ordered jagged array of destinations sorted by type and travel time
     */
    public static String[][] getPlacesOrdered(String baseAddress,String[] destinations)
    {
    	return TravelTime(baseAddress, GetAddresses(baseAddress, destinations));
    }
    /**
     * This is a user method. Asks user for a starting address and destinations
     * Calls the getAddressMethod and Travel Times on each destination
     * Finds the particular establishment of a type with the fastest travel time and
     * adds that to a string array.
     * @return results An array that holds an establishment of each type with the fastest travel time 
     */
    public static String[] GetBestPlaces(String[][] destinationsWithTravelTimesAppended)
    {
    	
    	String[] fastestOfEach = new String[destinationsWithTravelTimesAppended.length];//create a list that is the length of the destinations array (since we only need one element per array)
    	for (int destinationType = 0; destinationType < fastestOfEach.length; destinationType++)
    	{
    		fastestOfEach[destinationType] = destinationsWithTravelTimesAppended[destinationType][0];//takes the top element (the fastest) from each destination
    	}
    	return fastestOfEach;
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
    	String[] startAddressArr = new String[1];//need an array of size 1 to hold the base address
    	startAddressArr[0] = startAddress;//set the only element to the base address
    	String[][] fastest = new String[destinations.length][];//fastest is the jagged array which will hold the ordered destinations
    	for (int destinationIndex = 0; destinationIndex < destinations.length; destinationIndex++)//go through each destination type
    	{
	    	try
	    	{
	    		DistanceMatrix distances = DistanceMatrixApi.getDistanceMatrix(context, startAddressArr, destinations[destinationIndex]).await();//get the matrix from google
	    		//TODO MAKE THIS ONE API CALL!!!
	    		distances = SortByFastest(distances);//call a method to sort this matrix
	    		fastest[destinationIndex] = new String[destinations[destinationIndex].length];//make fastest of a certain type equal to the length of  the number of destinations
	    		for (int place = 0; place < destinations[destinationIndex].length; place++)
	    		{
	    			fastest[destinationIndex][place] = destinations[destinationIndex][place] + " is " + distances.rows[0].elements[place].duration + " away";//set it equal to the travel time
	    		}//loading up the array based on this info
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
    	long minTime;//the current least amount of time required to travel to a destination type
    	int index;//what destination is currently being examined
    	long[] travelTimes = new long[matrix.destinationAddresses.length];//this array allows for easy comparisons of travel times
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
     * @return allResults 
     */
    public static String[][] GetAddresses(String baseLocation, String[] establishmentType)
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
