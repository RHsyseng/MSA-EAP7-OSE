package com.redhat.refarch.microservices.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.refarch.microservices.product.model.Keyword;
import com.redhat.refarch.microservices.product.model.Product;

public class Demo
{

	public static void populate() throws HttpErrorException, IOException
	{
		if( RestClient.getFeaturedProducts().isEmpty() )
		{
			//Map products to their category as you add them:
			Map<Long, List<Keyword>> skuKeywords = new HashMap<>();
			for( String[] data : readCSV() )
			{
				Product product = getProduct( data );
				long sku = RestClient.addProduct( product );
				List<Keyword> productKeywords = new ArrayList<>();
				skuKeywords.put( sku, productKeywords );
				String image = product.getImage();
				if( "TV".equals( image ) )
				{
					productKeywords.add( getKeyword( "Electronics" ) );
					productKeywords.add( getKeyword( "TV" ) );
				}
				else if( "Microwave".equals( image ) )
				{
					productKeywords.add( getKeyword( "Electronics" ) );
					productKeywords.add( getKeyword( "Microwave" ) );
				}
				else if( "Laptop".equals( image ) )
				{
					productKeywords.add( getKeyword( "Electronics" ) );
					productKeywords.add( getKeyword( "Laptop" ) );
				}
				else if( "CoffeeTable".equals( image ) )
				{
					productKeywords.add( getKeyword( "Furniture" ) );
					productKeywords.add( getKeyword( "Table" ) );
				}
			}
			//Get unique keywords:
			Set<Keyword> keywords = new HashSet<>();
			for( Entry<Long, List<Keyword>> entry : skuKeywords.entrySet() )
			{
				keywords.addAll( entry.getValue() );
			}
			//Store keywords in database:
			for( Keyword keyword : keywords )
			{
				RestClient.addKeyword( keyword );
			}
			//Classify products:
			for( Entry<Long, List<Keyword>> entry : skuKeywords.entrySet() )
			{
				RestClient.classifyProduct( entry.getKey(), entry.getValue() );
			}
		}
		else
		{
			logInfo( "Product database is not empty, will not populate demo products!" );
		}
	}

	private static Keyword getKeyword(String word) {
		Keyword keyword = new Keyword();
		keyword.setKeyword( word );
		return keyword;
	}

	private static Product getProduct(String[] data)
	{
		Product product = new Product();
		product.setDescription( data[0] );
		product.setHeight( Double.parseDouble( data[1] ) );
		product.setLength( Double.parseDouble( data[2] ) );
		product.setName( data[3] );
		product.setWeight( Double.parseDouble( data[4] ) );
		product.setWidth( Double.parseDouble( data[5] ) );
		product.setFeatured( Boolean.parseBoolean( data[6] ) );
		product.setAvailability( Integer.parseInt( data[7] ) );
		product.setImage( data[8] );
		product.setPrice( BigDecimal.valueOf( Double.parseDouble( data[9] ) ) );
		return product;
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
