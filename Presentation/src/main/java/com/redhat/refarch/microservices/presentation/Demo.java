package com.redhat.refarch.microservices.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Demo
{

	public static void populate() throws IOException, JSONException, URISyntaxException, HttpErrorException
	{
		if( RestClient.getFeaturedProducts().isEmpty() )
		{
			//Map products to their category as you add them:
			Map<Long, List<String>> skuKeywords = new HashMap<>();
			for( String[] data : readCSV() )
			{
				JSONObject jsonObject = getProduct( data );
				long sku = RestClient.addProduct( jsonObject );
				List<String> productKeywords = new ArrayList<>();
				skuKeywords.put( sku, productKeywords );
				String image = jsonObject.getString( "image" );
				if( "TV".equals( image ) )
				{
					productKeywords.add( "Electronics" );
					productKeywords.add( "TV" );
				}
				else if( "Microwave".equals( image ) )
				{
					productKeywords.add( "Electronics" );
					productKeywords.add( "Microwave" );
				}
				else if( "Laptop".equals( image ) )
				{
					productKeywords.add( "Electronics" );
					productKeywords.add( "Laptop" );
				}
				else if( "CoffeeTable".equals( image ) )
				{
					productKeywords.add( "Furniture" );
					productKeywords.add( "Table" );
				}
			}
			//Get unique keywords:
			Set<String> keywords = new HashSet<>();
			for( Entry<Long, List<String>> entry : skuKeywords.entrySet() )
			{
				keywords.addAll( entry.getValue() );
			}
			//Store keywords in database:
			for( String keyword : keywords )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put( "keyword", keyword );
				RestClient.addKeyword( jsonObject );
			}
			//Classify products:
			for( Entry<Long, List<String>> entry : skuKeywords.entrySet() )
			{
				RestClient.classifyProduct( entry.getKey(), entry.getValue() );
			}
		}
		else
		{
			logInfo( "Product database is not empty, will not populate demo products!" );
		}
	}

	private static JSONObject getProduct(String[] data) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put( "description", data[0] );
		jsonObject.put( "height", Double.parseDouble( data[1] ) );
		jsonObject.put( "length", Double.parseDouble( data[2] ) );
		jsonObject.put( "name", data[3] );
		jsonObject.put( "weight", Double.parseDouble( data[4] ) );
		jsonObject.put( "width", Double.parseDouble( data[5] ) );
		jsonObject.put( "featured", Boolean.parseBoolean( data[6] ) );
		jsonObject.put( "availability", Integer.parseInt( data[7] ) );
		jsonObject.put( "image", data[8] );
		jsonObject.put( "price", Double.parseDouble( data[9] ) );
		return jsonObject;
	}

	private static List<String[]> readCSV() throws IOException
	{
		InputStreamReader reader = new InputStreamReader( Demo.class.getResourceAsStream( "/demo-products.csv" ) );
		List<String[]> data = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader( reader );
		for( String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine() )
		{
			data.add( line.split( "," ) );
		}
		return data;
	}

	private static void logInfo(String message)
	{
		Logger.getLogger( Demo.class.getName() ).log( Level.INFO, message );
	}
}
